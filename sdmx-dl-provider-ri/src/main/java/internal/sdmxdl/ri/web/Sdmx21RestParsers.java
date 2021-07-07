package internal.sdmxdl.ri.web;

import internal.util.rest.MediaType;
import lombok.AccessLevel;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.FileParser;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.SdmxMediaType;
import sdmxdl.xml.stream.SdmxXmlStreams;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static internal.sdmxdl.ri.web.RiRestParsers.getResourceSelector;
import static java.util.Collections.singletonList;

@lombok.Builder
@lombok.AllArgsConstructor(access = AccessLevel.PUBLIC)
public class Sdmx21RestParsers implements RiRestParsers {

    @lombok.Singular
    private final Map<SdmxResourceType, MediaType> customTypes;

    @Override
    public @NonNull List<MediaType> getFlowsTypes() {
        return SdmxResourceType.DATAFLOW
                .applyOn(customTypes)
                .map(Collections::singletonList)
                .orElse(DEFAULT_DATAFLOW_TYPES);
    }

    @Override
    public @NonNull FileParser<List<Dataflow>> getFlowsParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs) {
        MediaType target = SdmxResourceType.DATAFLOW
                .applyOn(customTypes)
                .orElse(mediaType);

        if (target.isCompatible(STRUCT21) || target.isCompatible(GENERIC_XML)) {
            return SdmxXmlStreams.flow21(langs);
        }

        return new UnsupportedParser<>(target);
    }

    @Override
    public @NonNull List<MediaType> getFlowTypes() {
        return SdmxResourceType.DATAFLOW
                .applyOn(customTypes)
                .map(Collections::singletonList)
                .orElse(DEFAULT_DATAFLOW_TYPES);
    }

    @Override
    public @NonNull FileParser<Optional<Dataflow>> getFlowParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs, @NonNull DataflowRef ref) {
        MediaType target = SdmxResourceType.DATAFLOW
                .applyOn(customTypes)
                .orElse(mediaType);

        if (target.isCompatible(STRUCT21) || target.isCompatible(GENERIC_XML)) {
            return SdmxXmlStreams.flow21(langs).andThen(getResourceSelector(ref));
        }

        return new UnsupportedParser<>(target);
    }

    @Override
    public @NonNull List<MediaType> getStructureTypes() {
        return SdmxResourceType.DATASTRUCTURE
                .applyOn(customTypes)
                .map(Collections::singletonList)
                .orElse(DEFAULT_DATASTRUCTURE_TYPES);
    }

    @Override
    public @NonNull FileParser<Optional<DataStructure>> getStructureParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs, @NonNull DataStructureRef ref) {
        MediaType target = SdmxResourceType.DATASTRUCTURE
                .applyOn(customTypes)
                .orElse(mediaType);

        if (target.isCompatible(STRUCT21) || target.isCompatible(GENERIC_XML)) {
            return SdmxXmlStreams.struct21(langs).andThen(getResourceSelector(ref));
        }

        return new UnsupportedParser<>(target);
    }

    @Override
    public @NonNull List<MediaType> getDataTypes() {
        return SdmxResourceType.DATA
                .applyOn(customTypes)
                .map(Collections::singletonList)
                .orElse(DEFAULT_DATA_TYPES);
    }

    @Override
    public @NonNull FileParser<DataCursor> getDataParser(@NonNull MediaType mediaType, @NonNull DataStructure dsd, @NonNull ObsFactory dataFactory) {
        MediaType target = SdmxResourceType.DATA
                .applyOn(customTypes)
                .orElse(mediaType);

        if (target.isCompatible(GENERIC21)) {
            return SdmxXmlStreams.genericData21(dsd, dataFactory);
        }
        if (target.isCompatible(COMPACT21)) {
            return SdmxXmlStreams.compactData21(dsd, dataFactory);
        }
        if (target.isCompatible(GENERIC_XML)) {
            return SdmxXmlStreams.genericData21(dsd, dataFactory);
        }

        return new UnsupportedParser<>(target);
    }

    private static final MediaType GENERIC_XML = MediaType.parse(SdmxMediaType.GENERIC_XML);
    private static final MediaType STRUCT21 = MediaType.parse(SdmxMediaType.STRUCTURE_21);
    private static final MediaType GENERIC21 = MediaType.parse(SdmxMediaType.GENERIC_DATA_21);
    private static final MediaType COMPACT21 = MediaType.parse(SdmxMediaType.STRUCTURE_SPECIFIC_DATA_21);

    // NOTE: order matter for GENERIC_XML ! First generic, then compact
    @VisibleForTesting
    static final List<MediaType> DEFAULT_DATA_TYPES = Arrays.asList(GENERIC21, COMPACT21);

    @VisibleForTesting
    static final List<MediaType> DEFAULT_DATAFLOW_TYPES = singletonList(STRUCT21);

    @VisibleForTesting
    static final List<MediaType> DEFAULT_DATASTRUCTURE_TYPES = singletonList(STRUCT21);

    @VisibleForTesting
    @lombok.AllArgsConstructor
    static final class UnsupportedParser<T> implements FileParser<T> {

        @lombok.NonNull
        private final MediaType mediaType;

        @Override
        public @NonNull T parseStream(@NonNull InputStream resource) throws IOException {
            throw new IOException("Cannot parse media type '" + mediaType + "'");
        }
    }
}
