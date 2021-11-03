package internal.sdmxdl.ri.web;

import internal.util.http.MediaType;
import nbbrd.io.FileParser;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.util.SdmxFix;
import sdmxdl.xml.stream.SdmxXmlStreams;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static internal.sdmxdl.ri.web.RiHttpUtils.*;
import static java.util.Collections.singletonList;
import static sdmxdl.util.SdmxFix.Category.CONTENT;

public class DotStatRestParsers implements RiRestParsers {

    @Override
    public @NonNull List<MediaType> getFlowsTypes() {
        return singletonList(GENERIC_XML_TYPE);
    }

    @Override
    public @NonNull FileParser<List<Dataflow>> getFlowsParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs) {
        return SdmxXmlStreams.struct20(langs)
                .andThen(structs -> structs.stream().map(DotStatRestParsers::getFlowFromStructure).collect(Collectors.toList()));
    }

    @Override
    public @NonNull List<MediaType> getFlowTypes() {
        return singletonList(GENERIC_XML_TYPE);
    }

    @Override
    public @NonNull FileParser<Optional<Dataflow>> getFlowParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs, @NonNull DataflowRef ref) {
        return SdmxXmlStreams.struct20(langs)
                .andThen(structs -> structs.stream().map(DotStatRestParsers::getFlowFromStructure).findFirst());
    }

    @Override
    public @NonNull List<MediaType> getStructureTypes() {
        return singletonList(STRUCTURE_21_TYPE);
    }

    @Override
    public @NonNull FileParser<Optional<DataStructure>> getStructureParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs, @NonNull DataStructureRef ref) {
        return SdmxXmlStreams.struct20(langs)
                .andThen(structs -> structs.stream().findFirst());
    }

    @Override
    public @NonNull List<MediaType> getDataTypes() {
        return singletonList(STRUCTURE_SPECIFIC_DATA_20_TYPE);
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
