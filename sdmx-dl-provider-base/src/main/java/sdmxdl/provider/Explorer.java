package sdmxdl.provider;

import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import org.jspecify.annotations.Nullable;
import sdmxdl.*;
import sdmxdl.Flow;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public final class Explorer {

    private Explorer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Options that bound an exploration run so that a single slow or misbehaving source
     * cannot stall the whole overview.
     */
    @lombok.Value
    @lombok.Builder
    public static class Options {

        public static final Options DEFAULT = builder().build();

        /**
         * Maximum time to wait for a single source before reporting it as {@link Status#TIMEOUT}.
         */
        @lombok.NonNull
        @lombok.Builder.Default
        Duration perSourceTimeout = Duration.ofSeconds(60);

        /**
         * Overall wall-clock budget for the whole run; sources not completed within it are
         * reported as {@link Status#TIMEOUT}.
         */
        @lombok.NonNull
        @lombok.Builder.Default
        Duration totalBudget = Duration.ofMinutes(30);

        /**
         * Maximum number of sources probed concurrently.
         */
        @lombok.Builder.Default
        int maxConcurrency = Math.max(1, Runtime.getRuntime().availableProcessors());

        /**
         * Maximum number of flows probed per source (evenly spread over the discovered flows).
         * A higher value gives a more representative maturity signal at the cost of more requests.
         */
        @lombok.Builder.Default
        int maxFlowsSampled = 5;

        /**
         * Maximum number of candidate keys tried per flow before giving up on data.
         */
        @lombok.Builder.Default
        int maxKeysSampled = 2;
    }

    public static SortedMap<Status, List<Report>> explore(@NonNull SdmxWebManager manager, @NonNull Predicate<? super WebSource> filter) {
        return explore(manager, filter, Options.DEFAULT);
    }

    public static SortedMap<Status, List<Report>> explore(@NonNull SdmxWebManager manager, @NonNull Predicate<? super WebSource> filter, @NonNull Options options) {
        manager.getNetworking().warmupNetwork();
        List<WebSource> sources = manager.getSources()
                .values()
                .stream()
                .filter(filter)
                .collect(toList());
        return exploreAll(manager, sources, options)
                .stream()
                .collect(groupingBy(Report::getStatus, TreeMap::new, toList()));
    }

    private static List<Report> exploreAll(SdmxWebManager manager, List<WebSource> sources, Options options) {
        if (sources.isEmpty()) {
            return Collections.emptyList();
        }
        int parallelism = Math.max(1, Math.min(options.getMaxConcurrency(), sources.size()));
        ExecutorService executor = Executors.newFixedThreadPool(parallelism);
        try {
            Map<WebSource, Future<Report>> futures = new LinkedHashMap<>();
            for (WebSource source : sources) {
                futures.put(source, executor.submit(() -> explore(manager, source, options)));
            }
            long deadline = System.nanoTime() + clampToNanos(options.getTotalBudget());
            long perSourceNanos = clampToNanos(options.getPerSourceTimeout());
            List<Report> result = new ArrayList<>(sources.size());
            for (Map.Entry<WebSource, Future<Report>> entry : futures.entrySet()) {
                long remaining = deadline - System.nanoTime();
                long waitNanos = Math.min(perSourceNanos, Math.max(0, remaining));
                Future<Report> future = entry.getValue();
                try {
                    result.add(future.get(waitNanos, TimeUnit.NANOSECONDS));
                } catch (TimeoutException ex) {
                    future.cancel(true);
                    result.add(Report.of(entry.getKey(), Status.TIMEOUT, SourceRequest.builder().build()));
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    result.add(Report.of(entry.getKey(), Status.UNEXPECTED_FAILURE, cause, stackTraceToString(cause)));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    future.cancel(true);
                    result.add(Report.of(entry.getKey(), Status.TIMEOUT, SourceRequest.builder().build()));
                }
            }
            return result;
        } finally {
            executor.shutdownNow();
        }
    }

    private static long clampToNanos(Duration duration) {
        try {
            return duration.toNanos();
        } catch (ArithmeticException overflow) {
            return Long.MAX_VALUE;
        }
    }

    public static @NonNull Report explore(@NonNull SdmxWebManager manager, @NonNull WebSource source) {
        return explore(manager, source, Options.DEFAULT);
    }

    public static @NonNull Report explore(@NonNull SdmxWebManager manager, @NonNull WebSource source, @NonNull Options options) {
        long start = System.nanoTime();
        Report report = doExplore(manager, source, options);
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000L;
        return report.withDurationMillis(elapsedMillis);
    }

    private static @NonNull Report doExplore(@NonNull SdmxWebManager manager, @NonNull WebSource source, @NonNull Options options) {
        try (Connection c = manager.getConnection(source, Languages.ANY)) {

            try {
                c.testConnection();
            } catch (Exception fatal) {
                return Report.of(source, Status.CONNECTION_FAILURE, fatal, SourceRequest.builder().build());
            }

            SourceRequest sourceRequest = SourceRequest.builder().build();
            List<DatabaseRef> databases;
            try {
                databases = c.getDatabases()
                        .stream()
                        .map(Database::getRef)
                        .sorted(comparing(Objects::toString))
                        .collect(toList());
            } catch (Exception fatal) {
                return Report.of(source, Status.DB_FAILURE, fatal, sourceRequest);
            }

            DatabaseRequest databaseRequest;
            List<FlowRef> flows;
            {
                Iterator<DatabaseRef> db = databases.isEmpty()
                        ? singletonList(DatabaseRef.NO_DATABASE).iterator()
                        : databases.stream().iterator();
                do {
                    databaseRequest = DatabaseRequest.builderOf(sourceRequest).database(db.next()).build();
                    try {
                        flows = c.getFlows(databaseRequest.getDatabase())
                                .stream()
                                .map(Flow::getRef)
                                .sorted(comparing(Objects::toString))
                                .collect(toList());
                    } catch (Exception fatal) {
                        return Report.of(source, Status.FLOW_FAILURE, fatal, databaseRequest)
                                .withCoverage(databases.size(), 0, 0, 0, 0);
                    }
                } while (db.hasNext() && flows.isEmpty());
            }

            if (flows.isEmpty()) {
                return Report.of(source, Status.NO_FLOW, databaseRequest)
                        .withCoverage(databases.size(), 0, 0, 0, 0);
            }

            // Sample several flows (not just the first) so the report reflects how broadly the
            // driver works, not whether a single lucky flow happens to succeed.
            List<FlowRef> sample = sampleFlows(flows, options.getMaxFlowsSampled());
            int flowsWithStructure = 0;
            int flowsWithData = 0;
            Report best = null;
            for (FlowRef flowRef : sample) {
                FlowRequest flowRequest = FlowRequest.builderOf(databaseRequest).flow(flowRef).build();
                Report outcome = exploreFlow(c, source, flowRequest, options);
                if (hasStructure(outcome.getStatus())) {
                    flowsWithStructure++;
                }
                if (outcome.getStatus() == Status.SUCCESS) {
                    flowsWithData++;
                }
                // Keep the furthest-stage outcome as the representative status for the source.
                if (best == null || outcome.getStatus().ordinal() > best.getStatus().ordinal()) {
                    best = outcome;
                }
            }

            if (best == null) {
                // Defensive: sample is never empty here (flows is non-empty), but keep a safe fallback.
                return Report.of(source, Status.NO_FLOW, databaseRequest)
                        .withCoverage(databases.size(), flows.size(), 0, 0, 0);
            }

            return best.withCoverage(databases.size(), flows.size(), sample.size(), flowsWithStructure, flowsWithData);

        } catch (Throwable fatal) {
            return Report.of(source, Status.UNEXPECTED_FAILURE, fatal, stackTraceToString(fatal));
        }
    }

    private static Report exploreFlow(Connection c, WebSource source, FlowRequest flowRequest, Options options) {
        MetaSet metaSet;
        try {
            metaSet = c.getMeta(flowRequest.getDatabase(), flowRequest.getFlow());
        } catch (Exception fatal) {
            return Report.of(source, Status.META_FAILURE, fatal, flowRequest);
        }

        if (isInvalidStructure(metaSet)) {
            return Report.of(source, Status.NO_META, flowRequest);
        }

        List<Key> keys = candidateKeys(c, flowRequest, metaSet.getStructure(), options);
        KeyRequest keyRequest;
        DataSet dataSet;
        Iterator<Key> key = keys.iterator();
        do {
            keyRequest = KeyRequest.builderOf(flowRequest).key(key.next()).build();
            try {
                dataSet = c.getData(keyRequest.getDatabase(), keyRequest.getFlow(), keyRequest.toQuery());
            } catch (Exception fatal) {
                return Report.of(source, Status.DATA_FAILURE, fatal, keyRequest);
            }
        } while (key.hasNext() && dataSet.getData().isEmpty());

        if (dataSet.getData().isEmpty()) {
            return Report.of(source, Status.NO_DATA, keyRequest);
        }

        return Report.of(source, Status.SUCCESS, keyRequest);
    }

    // A structure was usable (coded dimensions) whenever the data stage was reached.
    private static boolean hasStructure(Status status) {
        return status == Status.DATA_FAILURE || status == Status.NO_DATA || status == Status.SUCCESS;
    }

    public enum Status {
        UNEXPECTED_FAILURE,
        TIMEOUT,
        CONNECTION_FAILURE,
        DB_FAILURE,
        FLOW_FAILURE,
        NO_FLOW,
        META_FAILURE,
        NO_META,
        DATA_FAILURE,
        NO_DATA,
        SUCCESS
    }

    @lombok.Value(staticConstructor = "of")
    public static class Report {

        @StaticFactoryMethod
        public static @NonNull Report of(@NonNull WebSource source, @NonNull Status status, @NonNull Throwable fatal, @NonNull Object request) {
            return new Report(source.getId(), status, request, fatal.getClass().getSimpleName(), fatal.getMessage(), 0, 0, 0, 0, 0, 0);
        }

        @StaticFactoryMethod
        public static @NonNull Report of(@NonNull WebSource source, @NonNull Status status, @NonNull Object request) {
            return new Report(source.getId(), status, request, null, null, 0, 0, 0, 0, 0, 0);
        }

        @NonNull
        String source;

        @NonNull
        Status status;

        @NonNull
        Object request;

        @Nullable
        String error;

        @Nullable
        String message;

        /**
         * Total time spent probing the source, in milliseconds (0 when not measured).
         */
        @lombok.With
        long durationMillis;

        /**
         * Number of databases discovered on the source.
         */
        int databaseCount;

        /**
         * Number of flows discovered in the probed database.
         */
        int flowCount;

        /**
         * Number of flows actually sampled (bounded by {@link Options#getMaxFlowsSampled()}).
         */
        int flowsSampled;

        /**
         * Number of sampled flows that yielded a usable (coded) structure.
         */
        int flowsWithStructure;

        /**
         * Number of sampled flows that returned at least one observation.
         */
        int flowsWithData;

        @NonNull
        Report withCoverage(int databaseCount, int flowCount, int flowsSampled, int flowsWithStructure, int flowsWithData) {
            return new Report(source, status, request, error, message, durationMillis,
                    databaseCount, flowCount, flowsSampled, flowsWithStructure, flowsWithData);
        }

        /**
         * Formats a single human-readable line describing this report, shared by all consumers.
         */
        public @NonNull String toSummaryLine() {
            return "[" + source + "] "
                    + "error=" + error + " "
                    + "message=" + message + " "
                    + "request=" + request + " "
                    + "coverage=" + flowsWithData + "/" + flowsWithStructure + "/" + flowsSampled + " of " + flowCount + " flows"
                    + (databaseCount > 0 ? ", " + databaseCount + " db" : "") + " "
                    + "(" + durationMillis + "ms)";
        }
    }

    private static boolean isInvalidStructure(MetaSet metaSet) {
        return metaSet.getStructure().getDimensions().stream()
                .allMatch(dimension -> dimension.getCodes().isEmpty());
    }

    // Evenly-spread, deterministic sample so runs are reproducible and not biased towards the
    // (alphabetically) first flows only.
    private static List<FlowRef> sampleFlows(List<FlowRef> flows, int max) {
        if (max <= 0 || flows.size() <= max) {
            return flows;
        }
        List<FlowRef> result = new ArrayList<>(max);
        double step = (double) flows.size() / max;
        for (int i = 0; i < max; i++) {
            result.add(flows.get((int) (i * step)));
        }
        return result;
    }

    // Candidate keys tried in order: a specific built key first (cheap), then the broad Key.ALL as
    // a fallback. The fallback matters when the built key is an impossible combination (e.g. sources
    // with mutually-exclusive dimensions), which would otherwise wrongly look like "no data".
    private static List<Key> candidateKeys(Connection c, FlowRequest flowRequest, Structure structure, Options options) {
        List<Key> result = new ArrayList<>();
        try {
            result.add(buildKey(c, flowRequest.getDatabase(), flowRequest.getFlow(), structure));
        } catch (Exception ignore) {
            // getAvailableDimensionCodes may be unsupported or fail; fall back to the broad query
            // below instead of misreporting this as a data failure.
        }
        if (!result.contains(Key.ALL)) {
            result.add(Key.ALL);
        }
        int max = Math.max(1, options.getMaxKeysSampled());
        return result.size() > max ? result.subList(0, max) : result;
    }

    private static Key buildKey(Connection c, DatabaseRef db, FlowRef flow, Structure structure) throws IOException {
        Key.Builder key = Key.builder(structure);
        List<Dimension> dimensions = structure.getDimensions();
        for (int i = 0; i < dimensions.size(); i++) {
            Iterator<String> availableDimensionCodes = c.getAvailableDimensionCodes(db, flow, key.build(), i).iterator();
            if (availableDimensionCodes.hasNext()) {
                key.put(dimensions.get(i).getId(), availableDimensionCodes.next());
            }
        }
        return key.build();
    }

    private static String stackTraceToString(Throwable e) {
        try (StringWriter sw = new StringWriter()) {
            try (PrintWriter pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
                return sw.toString();
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
