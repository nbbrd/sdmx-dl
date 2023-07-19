package sdmxdl.provider.ri.web;

import lombok.NonNull;
import nbbrd.io.FileParser;
import nbbrd.io.net.MediaType;
import nbbrd.io.xml.Xml;
import sdmxdl.*;
import sdmxdl.format.DataCursor;
import sdmxdl.format.ObsParser;
import sdmxdl.format.xml.SdmxXmlStreams;
import sdmxdl.format.xml.XmlMediaTypes;
import sdmxdl.provider.SdmxFix;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static sdmxdl.provider.SdmxFix.Category.CONTENT;

public class DotStatRestParsers implements RiRestParsers {

    public static final MediaType XML = Xml.APPLICATION_XML_UTF_8.withoutParameters();

    @Override
    public @NonNull List<MediaType> getFlowsTypes() {
        return singletonList(XML);
    }

    @Override
    public @NonNull FileParser<List<Dataflow>> getFlowsParser(@NonNull MediaType mediaType, @NonNull Languages langs) {
        return SdmxXmlStreams.struct20(langs)
                .andThen(structs -> structs.stream().map(DotStatRestParsers::getFlowFromStructure).collect(Collectors.toList()));
    }

    @Override
    public @NonNull List<MediaType> getFlowTypes() {
        return singletonList(XML);
    }

    @Override
    public @NonNull FileParser<Optional<Dataflow>> getFlowParser(@NonNull MediaType mediaType, @NonNull Languages langs, @NonNull DataflowRef ref) {
        return SdmxXmlStreams.struct20(langs)
                .andThen(structs -> structs.stream().map(DotStatRestParsers::getFlowFromStructure).findFirst());
    }

    @Override
    public @NonNull List<MediaType> getStructureTypes() {
        return singletonList(XmlMediaTypes.STRUCTURE_21);
    }

    @Override
    public @NonNull FileParser<Optional<DataStructure>> getStructureParser(@NonNull MediaType mediaType, @NonNull Languages langs, @NonNull DataStructureRef ref) {
        return SdmxXmlStreams.struct20(langs)
                .andThen(structs -> structs.stream().findFirst());
    }

    @Override
    public @NonNull List<MediaType> getDataTypes() {
        return singletonList(XmlMediaTypes.STRUCTURE_SPECIFIC_DATA_20);
    }

    @SdmxFix(id = 1, category = CONTENT, cause = "Time dimension is always TIME in data")
    @Override
    public @NonNull FileParser<DataCursor> getDataParser(@NonNull MediaType mediaType, @NonNull DataStructure dsd, @NonNull Supplier<ObsParser> dataFactory) {
        DataStructure modifiedDsd = dsd.toBuilder().timeDimensionId("TIME").build();
        return SdmxXmlStreams.compactData20(modifiedDsd, dataFactory);
    }

    @Override
    public @NonNull List<MediaType> getCodelistTypes() {
        throw new UnsupportedOperationException("codelist");
    }

    @Override
    public @NonNull FileParser<Optional<Codelist>> getCodelistParser(@NonNull MediaType mediaType, @NonNull Languages langs, @NonNull CodelistRef ref) {
        throw new UnsupportedOperationException("codelist");
    }

    public static @NonNull Dataflow getFlowFromStructure(@NonNull DataStructure o) {
        return Dataflow.builder().ref(getFlowRefFromStructureRef(o.getRef())).structureRef(o.getRef()).name(o.getName()).build();
    }

    public static @NonNull DataflowRef getFlowRefFromStructureRef(@NonNull DataStructureRef o) {
        return DataflowRef.of(o.getAgency(), o.getId(), o.getVersion());
    }
}
