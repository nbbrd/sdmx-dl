package internal.sdmxdl.ri.web;

import internal.util.http.MediaType;
import nbbrd.io.FileParser;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.SdmxMediaType;
import sdmxdl.util.SdmxFix;
import sdmxdl.xml.stream.SdmxXmlStreams;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static sdmxdl.util.SdmxFix.Category.CONTENT;

public class DotStatRestParsers implements RiRestParsers {

    private static final MediaType FLOW20 = MediaType.parse(SdmxMediaType.GENERIC_XML);
    private static final MediaType STRUCT20 = MediaType.parse(SdmxMediaType.STRUCTURE_21);
    private static final MediaType COMPACT20 = MediaType.parse(SdmxMediaType.STRUCTURE_SPECIFIC_DATA_20);

    @Override
    public @NonNull List<MediaType> getFlowsTypes() {
        return Collections.singletonList(FLOW20);
    }

    @Override
    public @NonNull FileParser<List<Dataflow>> getFlowsParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs) {
        return SdmxXmlStreams.struct20(langs)
                .andThen(structs -> structs.stream().map(DotStatRestParsers::getFlowFromStructure).collect(Collectors.toList()));
    }

    @Override
    public @NonNull List<MediaType> getFlowTypes() {
        return Collections.singletonList(FLOW20);
    }

    @Override
    public @NonNull FileParser<Optional<Dataflow>> getFlowParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs, @NonNull DataflowRef ref) {
        return SdmxXmlStreams.struct20(langs)
                .andThen(structs -> structs.stream().map(DotStatRestParsers::getFlowFromStructure).findFirst());
    }

    @Override
    public @NonNull List<MediaType> getStructureTypes() {
        return Collections.singletonList(STRUCT20);
    }

    @Override
    public @NonNull FileParser<Optional<DataStructure>> getStructureParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs, @NonNull DataStructureRef ref) {
        return SdmxXmlStreams.struct20(langs)
                .andThen(structs -> structs.stream().findFirst());
    }

    @Override
    public @NonNull List<MediaType> getDataTypes() {
        return Collections.singletonList(COMPACT20);
    }

    @SdmxFix(id = 1, category = CONTENT, cause = "Time dimension is always TIME in data")
    @Override
    public @NonNull FileParser<DataCursor> getDataParser(@NonNull MediaType mediaType, @NonNull DataStructure dsd, @NonNull ObsFactory dataFactory) {
        DataStructure modifiedDsd = dsd.toBuilder().timeDimensionId("TIME").build();
        return SdmxXmlStreams.compactData20(modifiedDsd, dataFactory);
    }

    @Override
    public @NonNull List<MediaType> getCodelistTypes() {
        throw new UnsupportedOperationException("codelist");
    }

    @Override
    public @NonNull FileParser<Optional<Codelist>> getCodelistParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs, @NonNull CodelistRef ref) {
        throw new UnsupportedOperationException("codelist");
    }

    public static @NonNull Dataflow getFlowFromStructure(@NonNull DataStructure o) {
        return Dataflow.of(getFlowRefFromStructureRef(o.getRef()), o.getRef(), o.getLabel());
    }

    public static @NonNull DataflowRef getFlowRefFromStructureRef(@NonNull DataStructureRef o) {
        return DataflowRef.of(o.getAgency(), o.getId(), o.getVersion());
    }
}
