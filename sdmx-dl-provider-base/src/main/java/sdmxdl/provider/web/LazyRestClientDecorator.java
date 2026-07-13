package sdmxdl.provider.web;

import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.Marker;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

@lombok.RequiredArgsConstructor
final class LazyRestClientDecorator implements RestClientDecorator {

    private final @NonNull Supplier<RestClient> supplier;

    @lombok.Getter(lazy = true)
    private final @NonNull RestClient decorated = supplier.get();

    @Override
    public @NonNull List<Flow> getFlows() throws IOException {
        return getDecorated().getFlows();
    }

    @Override
    public @NonNull Structure getStructure(@NonNull StructureRef ref) throws IOException {
        return getDecorated().getStructure(ref);
    }

    @Override
    public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull Structure dsd) throws IOException {
        return getDecorated().getData(ref, dsd);
    }

    @Override
    public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) throws IOException {
        return getDecorated().getCodelist(ref);
    }

    @Override
    public @NonNull Set<Feature> getSupportedFeatures() throws IOException {
        return getDecorated().getSupportedFeatures();
    }

    @Override
    public @NonNull Optional<URI> testClient() throws IOException {
        return getDecorated().testClient();
    }

    @Override
    public @NonNull Marker getMarker() {
        return getDecorated().getMarker();
    }
}
