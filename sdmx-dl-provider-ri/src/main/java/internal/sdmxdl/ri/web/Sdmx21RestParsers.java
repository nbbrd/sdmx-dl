package internal.sdmxdl.ri.web;

import internal.util.http.MediaType;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.FileParser;
import nbbrd.io.xml.Xml;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.xml.DataCursor;
import sdmxdl.xml.stream.SdmxXmlStreams;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static internal.sdmxdl.ri.web.RiHttpUtils.*;
import static internal.sdmxdl.ri.web.RiRestParsers.getResourceSelector;
import static java.util.Collections.singletonList;

public class Sdmx21RestParsers implements RiRestParsers {

    @Override
    public @NonNull List<MediaType> getFlowsTypes() {
        return DEFAULT_DATAFLOW_TYPES;
    }

    @Override
    public @NonNull FileParser<List<Dataflow>> getFlowsParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs) {

        if (mediaType.isCompatibleWithoutParameters(STRUCTURE_21_TYPE) || mediaType.isCompatible(GENERIC_XML_TYPE)) {
            return withCharset(SdmxXmlStreams.flow21(langs), mediaType.getCharset());
        }

        return new UnsupportedParser<>(mediaType);
    }

    @Override
    public @NonNull List<MediaType> getFlowTypes() {
        return DEFAULT_DATAFLOW_TYPES;
    }

    @Override
    public @NonNull FileParser<Optional<Dataflow>> getFlowParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs, @NonNull DataflowRef ref) {

        if (mediaType.isCompatibleWithoutParameters(STRUCTURE_21_TYPE) || mediaType.isCompatible(GENERIC_XML_TYPE)) {
            return withCharset(SdmxXmlStreams.flow21(langs).andThen(getResourceSelector(ref)), mediaType.getCharset());
        }

        return new UnsupportedParser<>(mediaType);
    }

    @Override
    public @NonNull List<MediaType> getStructureTypes() {
        return DEFAULT_DATASTRUCTURE_TYPES;
    }

    @Override
    public @NonNull FileParser<Optional<DataStructure>> getStructureParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs, @NonNull DataStructureRef ref) {

        if (mediaType.isCompatibleWithoutParameters(STRUCTURE_21_TYPE) || mediaType.isCompatible(GENERIC_XML_TYPE)) {
            return withCharset(SdmxXmlStreams.struct21(langs).andThen(getResourceSelector(ref)), mediaType.getCharset());
        }

        return new UnsupportedParser<>(mediaType);
    }

    @Override
    public @NonNull List<MediaType> getDataTypes() {
        return DEFAULT_DATA_TYPES;
    }

    @Override
    public @NonNull FileParser<DataCursor> getDataParser(@NonNull MediaType mediaType, @NonNull DataStructure dsd, @NonNull ObsFactory dataFactory) {

        if (mediaType.isCompatibleWithoutParameters(GENERIC_DATA_21_TYPE)) {
            return withCharset(SdmxXmlStreams.genericData21(dsd, dataFactory), mediaType.getCharset());
        }
        if (mediaType.isCompatibleWithoutParameters(STRUCTURE_SPECIFIC_DATA_21_TYPE)) {
            return withCharset(SdmxXmlStreams.compactData21(dsd, dataFactory), mediaType.getCharset());
        }
        if (mediaType.isCompatible(GENERIC_XML_TYPE)) {
            return withCharset(SdmxXmlStreams.genericData21(dsd, dataFactory), mediaType.getCharset());
        }

        return new UnsupportedParser<>(mediaType);
    }

    @Override
    public @NonNull List<MediaType> getCodelistTypes() {
        return DEFAULT_DATASTRUCTURE_TYPES;
    }

    @Override
    public @NonNull FileParser<Optional<Codelist>> getCodelistParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs, @NonNull CodelistRef ref) {

        if (mediaType.isCompatibleWithoutParameters(STRUCTURE_21_TYPE) || mediaType.isCompatible(GENERIC_XML_TYPE)) {
            return withCharset(SdmxXmlStreams.codelist21(langs).andThen(getResourceSelector(ref)), mediaType.getCharset());
        }

        return new UnsupportedParser<>(mediaType);
    }

    // NOTE: order matter for GENERIC_XML ! First generic, then compact
    @VisibleForTesting
    static final List<MediaType> DEFAULT_DATA_TYPES = Arrays.asList(GENERIC_DATA_21_TYPE, STRUCTURE_SPECIFIC_DATA_21_TYPE);

    @VisibleForTesting
    static final List<MediaType> DEFAULT_DATAFLOW_TYPES = singletonList(STRUCTURE_21_TYPE);

    @VisibleForTesting
    static final List<MediaType> DEFAULT_DATASTRUCTURE_TYPES = singletonList(STRUCTURE_21_TYPE);

    @VisibleForTesting
    @lombok.AllArgsConstructor
    static final class UnsupportedParser<T> implements FileParser<T> {

        @lombok.NonNull
        private final MediaType mediaType;

        @Override
        public @NonNull T parseStream(@NonNull InputStream resource) throws IOException {
            throw new IOException("Unsupported media type '" + mediaType + "'");
        }

        @Override
        public @NonNull <V> FileParser<V> andThen(@NonNull Function<? super T, ? extends V> after) {
            return new UnsupportedParser<>(mediaType);
        }
    }

    //    @MightBePromoted
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> FileParser<T> withCharset(Xml.Parser<T> parser, Optional<Charset> charset) {
        return charset.isPresent() ? parser.withCharset(charset.get()) : parser;
    }
}
