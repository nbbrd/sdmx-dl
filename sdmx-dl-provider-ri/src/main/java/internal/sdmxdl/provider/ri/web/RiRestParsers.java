package internal.sdmxdl.provider.ri.web;

import lombok.NonNull;
import nbbrd.io.FileParser;
import nbbrd.io.function.IOFunction;
import nbbrd.io.net.MediaType;
import sdmxdl.*;
import sdmxdl.format.ObsParser;
import sdmxdl.format.DataCursor;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public interface RiRestParsers {

    @NonNull List<MediaType> getFlowsTypes();

    @NonNull FileParser<List<Dataflow>> getFlowsParser(@NonNull MediaType mediaType, @NonNull Languages langs);

    @NonNull List<MediaType> getFlowTypes();

    @NonNull FileParser<Optional<Dataflow>> getFlowParser(@NonNull MediaType mediaType, @NonNull Languages langs, @NonNull DataflowRef ref);

    @NonNull List<MediaType> getStructureTypes();

    @NonNull FileParser<Optional<DataStructure>> getStructureParser(@NonNull MediaType mediaType, @NonNull Languages langs, @NonNull DataStructureRef ref);

    @NonNull List<MediaType> getDataTypes();

    @NonNull FileParser<DataCursor> getDataParser(@NonNull MediaType mediaType, @NonNull DataStructure dsd, @NonNull Supplier<ObsParser> dataFactory);

    @NonNull List<MediaType> getCodelistTypes();

    @NonNull FileParser<Optional<Codelist>> getCodelistParser(@NonNull MediaType mediaType, @NonNull Languages langs, @NonNull CodelistRef ref);

    static <T extends Resource<R>, R extends ResourceRef<R>> @NonNull IOFunction<List<T>, Optional<T>> getResourceSelector(@NonNull R ref) {
        return list -> list
                .stream()
                .filter(ref::containsRef)
                .min(Comparator.comparing(ref::equalsRef).thenComparing(resource -> resource.getRef().toString()));
    }
}
