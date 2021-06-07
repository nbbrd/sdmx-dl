package internal.sdmxdl.ri.web;

import nbbrd.io.FileParser;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.util.MediaType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface RiRestParsers {

    @NonNull List<MediaType> getFlowsTypes();

    @NonNull FileParser<List<Dataflow>> getFlowsParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs);

    @NonNull List<MediaType> getFlowTypes();

    @NonNull FileParser<Optional<Dataflow>> getFlowParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs, @NonNull DataflowRef ref);

    @NonNull List<MediaType> getStructureTypes();

    @NonNull FileParser<Optional<DataStructure>> getStructureParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs, @NonNull DataStructureRef ref);

    @NonNull List<MediaType> getDataTypes();

    @NonNull FileParser<DataCursor> getDataParser(@NonNull MediaType mediaType, @NonNull DataStructure dsd, @NonNull ObsFactory dataFactory);

    static @NonNull List<MediaType> typesOf(@NonNull String... mediaTypes) {
        return Stream.of(mediaTypes).map(MediaType::parse).collect(Collectors.toList());
    }
}
