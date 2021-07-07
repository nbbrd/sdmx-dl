package internal.sdmxdl.ri.web;

import internal.util.rest.MediaType;
import lombok.AccessLevel;
import nbbrd.design.MightBePromoted;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.FileParser;
import nbbrd.io.function.IOSupplier;
import nbbrd.io.xml.Xml;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.SdmxMediaType;
import sdmxdl.xml.stream.SdmxXmlStreams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

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
            return withCharset(SdmxXmlStreams.flow21(langs), target.getCharset());
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
            return withCharset(SdmxXmlStreams.flow21(langs).andThen(getResourceSelector(ref)), target.getCharset());
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
            return withCharset(SdmxXmlStreams.struct21(langs).andThen(getResourceSelector(ref)), target.getCharset());
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
            return withCharset(SdmxXmlStreams.genericData21(dsd, dataFactory), target.getCharset());
        }
        if (target.isCompatible(COMPACT21)) {
            return withCharset(SdmxXmlStreams.compactData21(dsd, dataFactory), target.getCharset());
        }
        if (target.isCompatible(GENERIC_XML)) {
            return withCharset(SdmxXmlStreams.genericData21(dsd, dataFactory), target.getCharset());
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

        @Override
        public @NonNull <V> FileParser<V> andThen(@NonNull Function<? super T, ? extends V> after) {
            return new UnsupportedParser<>(mediaType);
        }
    }

    private static <T> FileParser<T> withCharset(Xml.Parser<T> parser, Optional<Charset> charset) {
        return charset.isPresent() ? new CharsetParser<>(parser, charset.get()) : parser;
    }

    @MightBePromoted
    @VisibleForTesting
    @lombok.AllArgsConstructor
    static final class CharsetParser<T> implements FileParser<T> {

        @lombok.NonNull
        private final Xml.Parser<T> delegate;

        @lombok.NonNull
        private final Charset charset;

        @Override
        public @NonNull T parseFile(@NonNull File source) throws IOException {
            return delegate.parseFile(source, charset);
        }

        @Override
        public @NonNull T parsePath(@NonNull Path source) throws IOException {
            return delegate.parsePath(source, charset);
        }

        @Override
        public @NonNull T parseResource(@NonNull Class<?> type, @NonNull String name) throws IOException {
            return delegate.parseResource(type, name, charset);
        }

        @Override
        public @NonNull T parseStream(IOSupplier<? extends InputStream> source) throws IOException {
            return delegate.parseStream(source, charset);
        }

        @Override
        public @NonNull T parseStream(@NonNull InputStream inputStream) throws IOException {
            return delegate.parseStream(inputStream, charset);
        }

        @Override
        public @NonNull <V> FileParser<V> andThen(@NonNull Function<? super T, ? extends V> after) {
            return new CharsetParser<>(delegate.andThen(after), charset);
        }
    }
}
