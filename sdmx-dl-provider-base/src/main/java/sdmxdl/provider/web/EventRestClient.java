package sdmxdl.provider.web;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.*;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.Marker;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * Decorator that emits execution plan events for query operations and result summaries.
 */
final class EventRestClient implements RestClient {

    private static final String QUERY_MARKER = "QUERY";
    private static final String SUMMARY_MARKER = "SUMMARY";

    static @NonNull RestClient of(@NonNull RestClient delegate, @Nullable EventListener onEvent) {
        return onEvent != null ? new EventRestClient(delegate, onEvent) : delegate;
    }

    @lombok.NonNull
    private final RestClient delegate;

    @lombok.NonNull
    private final EventListener onEvent;

    private final long connectionStart;
    private final AtomicLong networkMs = new AtomicLong();
    private final AtomicLong queryCount = new AtomicLong();

    EventRestClient(@NonNull RestClient delegate, @NonNull EventListener onEvent) {
        this.delegate = delegate;
        this.onEvent = onEvent;
        this.connectionStart = System.currentTimeMillis();
    }

    @Override
    public @NonNull Marker getMarker() {
        return delegate.getMarker();
    }

    @Override
    public @NonNull List<Flow> getFlows() throws IOException {
        onEvent.accept(QUERY_MARKER, WebEvents.onFlowsQuery());
        long start = System.currentTimeMillis();
        List<Flow> result = delegate.getFlows();
        long elapsed = System.currentTimeMillis() - start;
        networkMs.addAndGet(elapsed);
        queryCount.incrementAndGet();
        onEvent.accept(QUERY_MARKER, String.format(Locale.ROOT, "Got %d flows (%dms)", result.size(), elapsed));
        return result;
    }

    @Override
    public @NonNull Structure getStructure(@NonNull StructureRef ref) throws IOException {
        onEvent.accept(QUERY_MARKER, WebEvents.onStructureQuery(ref));
        long start = System.currentTimeMillis();
        Structure result = delegate.getStructure(ref);
        long elapsed = System.currentTimeMillis() - start;
        networkMs.addAndGet(elapsed);
        queryCount.incrementAndGet();
        onEvent.accept(QUERY_MARKER, String.format(Locale.ROOT, "Got structure with %d dimensions (%dms)", result.getDimensions().size(), elapsed));
        return result;
    }

    @Override
    public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull Structure dsd) throws IOException {
        onEvent.accept(QUERY_MARKER, WebEvents.onDataQuery(ref));
        long start = System.currentTimeMillis();
        Stream<Series> result = delegate.getData(ref, dsd);
        AtomicLong seriesCount = new AtomicLong();
        AtomicLong obsCount = new AtomicLong();
        queryCount.incrementAndGet();
        return result
                .peek(series -> {
                    seriesCount.incrementAndGet();
                    obsCount.addAndGet(series.getObs().size());
                })
                .onClose(() -> {
                    long elapsed = System.currentTimeMillis() - start;
                    networkMs.addAndGet(elapsed);
                    onEvent.accept(QUERY_MARKER, WebEvents.onDataReceived(seriesCount.get(), obsCount.get(), elapsed));
                });
    }

    @Override
    public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) throws IOException {
        return delegate.getCodelist(ref);
    }

    @Override
    public @NonNull Set<Feature> getSupportedFeatures() throws IOException {
        return delegate.getSupportedFeatures();
    }

    @NonNull
    @Override
    public Optional<URI> testClient() throws IOException {
        return delegate.testClient();
    }

    void emitSummary() {
        long totalElapsed = System.currentTimeMillis() - connectionStart;
        onEvent.accept(SUMMARY_MARKER, String.format(Locale.ROOT,
                "Completed %d %s in %dms",
                queryCount.get(),
                queryCount.get() == 1 ? "query" : "queries",
                totalElapsed));
    }
}

