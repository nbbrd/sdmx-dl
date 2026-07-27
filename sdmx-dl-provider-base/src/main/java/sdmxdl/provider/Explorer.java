package sdmxdl.provider;

import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import org.jspecify.annotations.Nullable;
import sdmxdl.*;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public final class Explorer {

    private Explorer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static SortedMap<Status, List<Report>> explore(@NonNull SdmxWebManager manager, @NonNull Predicate<? super WebSource> filter) {
        manager.getNetworking().warmupNetwork();
        return manager.getSources()
                .values()
                .parallelStream()
                .filter(filter)
                .map(source -> explore(manager, source))
                .collect(groupingBy(Explorer.Report::getStatus, TreeMap::new, toList()));
    }

    public static @NonNull Report explore(@NonNull SdmxWebManager manager, @NonNull WebSource source) {
        try (Connection c = manager.getConnection(source, Languages.ANY)) {

            try {
                c.testConnection();
            } catch (Exception fatal) {
                return Report.of(source, Status.CONNECTION_FAILURE, fatal, SourceRequest.builder().build());
            }

            SourceRequest sourceRequest;
            List<DatabaseRef> databases;
            {
                sourceRequest = SourceRequest.builder().build();
                try {
                    databases = c.getDatabases()
                            .stream()
                            .map(Database::getRef)
                            .sorted(comparing(Objects::toString))
                            .collect(toList());
                } catch (Exception fatal) {
                    return Report.of(source, Status.DB_FAILURE, fatal, sourceRequest);
                }
            }

//        if (databases.isEmpty()) {
//            return Report.of(source, Status.NO_DB);
//        }

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
                        return Report.of(source, Status.FLOW_FAILURE, fatal, databaseRequest);
                    }
                } while (db.hasNext() && flows.isEmpty());
            }

            if (flows.isEmpty()) {
                return Report.of(source, Status.NO_FLOW);
            }

            FlowRequest flowRequest;
            MetaSet metaSet;
            {
                Iterator<FlowRef> flow = flows.iterator();
                do {
                    flowRequest = FlowRequest.builderOf(databaseRequest).flow(flow.next()).build();
                    try {
                        metaSet = c.getMeta(flowRequest.getDatabase(), flowRequest.getFlow());
                    } catch (Exception fatal) {
                        return Report.of(source, Status.META_FAILURE, fatal, flowRequest);
                    }
                } while (flow.hasNext() && isInvalidStructure(metaSet));
            }

            if (isInvalidStructure(metaSet)) {
                return Report.of(source, Status.NO_META);
            }

            KeyRequest keyRequest;
            DataSet dataSet;
            {
                Iterator<Key> key = keyIterator(c, flowRequest.getDatabase(), flowRequest.getFlow(), metaSet.getStructure());
                do {
                    keyRequest = KeyRequest.builderOf(flowRequest).key(key.next()).build();
                    try {
                        dataSet = c.getData(keyRequest.getDatabase(), keyRequest.getFlow(), keyRequest.toQuery());
                    } catch (Exception fatal) {
                        return Report.of(source, Status.DATA_FAILURE, fatal, keyRequest);
                    }
                } while (key.hasNext() && dataSet.getData().isEmpty());
            }

            if (dataSet.getData().isEmpty()) {
                return Report.of(source, Status.NO_DATA);
            }

            return Report.of(source, Status.SUCCESS);

        } catch (Exception fatal) {
            return Report.of(source, Status.UNEXPECTED_FAILURE, fatal, stackTraceToString(fatal));
        }
    }

    public enum Status {
        UNEXPECTED_FAILURE,
        CONNECTION_FAILURE,
        DB_FAILURE,
        NO_DB,
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
        public static @NonNull Report of(@NonNull WebSource source, @NonNull Status status, @NonNull Exception fatal, @NonNull Object request) {
            return new Report(source.getId(), status, request, fatal.getClass().getSimpleName(), fatal.getMessage());
        }

        @StaticFactoryMethod
        public static @NonNull Report of(@NonNull WebSource source, @NonNull Status status) {
            return new Report(source.getId(), status, null, null, null);
        }

        @NonNull
        String source;

        @NonNull
        Status status;

        @Nullable
        Object request;

        @Nullable
        String error;

        @Nullable
        String message;
    }

    private static boolean isInvalidStructure(MetaSet metaSet) {
        return metaSet.getStructure().getDimensions().stream()
                .allMatch(dimension -> dimension.getCodes().isEmpty());
    }

    private static Iterator<Key> keyIterator(Connection c, DatabaseRef db, FlowRef flow, Structure structure) throws IOException {
        Key.Builder key = Key.builder(structure);
        List<Dimension> dimensions = structure.getDimensions();
        for (int i = 0; i < dimensions.size(); i++) {
            Iterator<String> availableDimensionCodes = c.getAvailableDimensionCodes(db, flow, key.build(), i).iterator();
            if (availableDimensionCodes.hasNext()) {
                key.put(dimensions.get(i).getId(), availableDimensionCodes.next());
            }
        }
        return singletonList(key.build()).iterator();
    }

    private static String stackTraceToString(Exception e) {
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
