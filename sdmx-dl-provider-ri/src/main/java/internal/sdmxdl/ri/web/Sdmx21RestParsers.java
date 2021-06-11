package internal.sdmxdl.ri.web;

import nbbrd.io.FileParser;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.SdmxMediaType;
import internal.util.rest.MediaType;
import sdmxdl.xml.stream.SdmxXmlStreams;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Sdmx21RestParsers implements RiRestParsers {

    private static final MediaType FLOW21 = MediaType.parse(SdmxMediaType.XML);
    private static final MediaType STRUCT21 = MediaType.parse(SdmxMediaType.STRUCTURE_21);
    private static final MediaType GENERIC21 = MediaType.parse(SdmxMediaType.GENERIC_DATA_21);
    private static final MediaType COMPACT21 = MediaType.parse(SdmxMediaType.STRUCTURE_SPECIFIC_DATA_21);

    @Override
    public @NonNull List<MediaType> getFlowsTypes() {
        return Collections.singletonList(FLOW21);
    }

    @Override
    public @NonNull FileParser<List<Dataflow>> getFlowsParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs) {
        return SdmxXmlStreams.flow21(langs);
    }

    @Override
    public @NonNull List<MediaType> getFlowTypes() {
        return Collections.singletonList(FLOW21);
    }

    @Override
    public @NonNull FileParser<Optional<Dataflow>> getFlowParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs, @NonNull DataflowRef ref) {
        return SdmxXmlStreams.flow21(langs)
                .andThen(flows -> flows.stream().filter(ref::containsRef).findFirst());
    }

    @Override
    public @NonNull List<MediaType> getStructureTypes() {
        return Collections.singletonList(STRUCT21);
    }

    @Override
    public @NonNull FileParser<Optional<DataStructure>> getStructureParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs, @NonNull DataStructureRef ref) {
        return SdmxXmlStreams.struct21(langs)
                .andThen(structs -> structs.stream().filter(ref::equalsRef).findFirst());
    }

    @Override
    public @NonNull List<MediaType> getDataTypes() {
        return Arrays.asList(COMPACT21, GENERIC21);
    }

    @Override
    public @NonNull FileParser<DataCursor> getDataParser(@NonNull MediaType mediaType, @NonNull DataStructure dsd, @NonNull ObsFactory dataFactory) {
        if (mediaType.isCompatible(GENERIC21)) {
            return SdmxXmlStreams.genericData21(dsd, dataFactory);
        }
        return SdmxXmlStreams.compactData21(dsd, dataFactory);
    }
}
