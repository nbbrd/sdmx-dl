package sdmxdl.provider.web;

import lombok.NonNull;
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
@lombok.RequiredArgsConstructor
final class EventRestClientDecorator implements RestClientDecorator {

    private static final String QUERY_MARKER = "QUERY";
    private static final int DEPTH = 0;

    @lombok.Getter
    @lombok.NonNull
    private final RestClient decorated;

    @lombok.NonNull
    private final EventListener onEvent;

    @Override
    public @NonNull Marker getMarker() {
        return decorated.getMarker();
    }

    @Override
    public @NonNull List<Flow> getFlows() throws IOException {
        onEvent.accept(QUERY_MARKER, WebEvents.onFlowsQuery(), DEPTH);
        long start = System.currentTimeMillis();
        List<Flow> result = decorated.getFlows();
        long elapsed = System.currentTimeMillis() - start;
        onEvent.accept(QUERY_MARKER, String.format(Locale.ROOT, "Got %d flows (%dms)", result.size(), elapsed), DEPTH);
        return result;
    }

    @Override
    public @NonNull Structure getStructure(@NonNull StructureRef ref) throws IOException {
        onEvent.accept(QUERY_MARKER, WebEvents.onStructureQuery(ref), DEPTH);
        long start = System.currentTimeMillis();
        Structure result = decorated.getStructure(ref);
        long elapsed = System.currentTimeMillis() - start;
        onEvent.accept(QUERY_MARKER, String.format(Locale.ROOT, "Got structure with %d dimensions (%dms)", result.getDimensions().size(), elapsed), DEPTH);
        return result;
    }

    @Override
    public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull Structure dsd) throws IOException {
        onEvent.accept(QUERY_MARKER, WebEvents.onDataQuery(ref), DEPTH);
        long start = System.currentTimeMillis();
        Stream<Series> result = decorated.getData(ref, dsd);
        AtomicLong seriesCount = new AtomicLong();
        AtomicLong obsCount = new AtomicLong();
        return result
                .peek(series -> {
                    seriesCount.incrementAndGet();
                    obsCount.addAndGet(series.getObs().size());
                })
                .onClose(() -> {
                    long elapsed = System.currentTimeMillis() - start;
                    onEvent.accept(QUERY_MARKER, WebEvents.onDataReceived(seriesCount.get(), obsCount.get(), elapsed), DEPTH);
                });
    }

    @Override
    public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) throws IOException {
        return decorated.getCodelist(ref);
    }

    @Override
    public @NonNull Set<Feature> getSupportedFeatures() throws IOException {
        return decorated.getSupportedFeatures();
    }

    @NonNull
    @Override
    public Optional<URI> testClient() throws IOException {
        return decorated.testClient();
    }
}
