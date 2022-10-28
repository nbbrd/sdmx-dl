package internal.sdmxdl.provider.ri.web;

import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.FileParser;
import nbbrd.io.function.IOFunction;
import nbbrd.io.net.MediaType;
import nbbrd.io.xml.Xml;
import sdmxdl.*;
import sdmxdl.format.DataCursor;
import sdmxdl.format.ObsParser;
import sdmxdl.format.xml.SdmxXmlStreams;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static internal.sdmxdl.provider.ri.web.RiRestParsers.getResourceSelector;
import static java.util.Collections.singletonList;
import static nbbrd.io.xml.Xml.APPLICATION_XML_UTF_8;
import static sdmxdl.format.xml.XmlMediaTypes.*;

public class Sdmx21RestParsers implements RiRestParsers {

    @Override
    public @NonNull List<MediaType> getFlowsTypes() {
        return DEFAULT_DATAFLOW_TYPES;
    }

    @Override
    public @NonNull FileParser<List<Dataflow>> getFlowsParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs) {

        if (mediaType.isCompatibleWithoutParameters(STRUCTURE_21) || mediaType.isCompatibleWithoutParameters(APPLICATION_XML_UTF_8)) {
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

        if (mediaType.isCompatibleWithoutParameters(STRUCTURE_21) || mediaType.isCompatibleWithoutParameters(APPLICATION_XML_UTF_8)) {
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

        if (mediaType.isCompatibleWithoutParameters(STRUCTURE_21) || mediaType.isCompatibleWithoutParameters(APPLICATION_XML_UTF_8)) {
            return withCharset(SdmxXmlStreams.struct21(langs).andThen(getResourceSelector(ref)), mediaType.getCharset());
        }

        return new UnsupportedParser<>(mediaType);
    }

    @Override
    public @NonNull List<MediaType> getDataTypes() {
        return DEFAULT_DATA_TYPES;
    }

    @Override
    public @NonNull FileParser<DataCursor> getDataParser(@NonNull MediaType mediaType, @NonNull DataStructure dsd, @NonNull Supplier<ObsParser> dataFactory) {

        if (mediaType.isCompatibleWithoutParameters(GENERIC_DATA_21)) {
            return withCharset(SdmxXmlStreams.genericData21(dsd, dataFactory), mediaType.getCharset());
        }
        if (mediaType.isCompatibleWithoutParameters(STRUCTURE_SPECIFIC_DATA_21)) {
            return withCharset(SdmxXmlStreams.compactData21(dsd, dataFactory), mediaType.getCharset());
        }
        if (mediaType.isCompatibleWithoutParameters(APPLICATION_XML_UTF_8)) {
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

        if (mediaType.isCompatibleWithoutParameters(STRUCTURE_21) || mediaType.isCompatibleWithoutParameters(APPLICATION_XML_UTF_8)) {
            return withCharset(SdmxXmlStreams.codelist21(langs).andThen(getResourceSelector(ref)), mediaType.getCharset());
        }

        return new UnsupportedParser<>(mediaType);
    }

    // NOTE: order matter for GENERIC_XML ! First generic, then compact
    @VisibleForTesting
    static final List<MediaType> DEFAULT_DATA_TYPES = Arrays.asList(GENERIC_DATA_21, STRUCTURE_SPECIFIC_DATA_21);

    @VisibleForTesting
    static final List<MediaType> DEFAULT_DATAFLOW_TYPES = singletonList(STRUCTURE_21);

    @VisibleForTesting
    static final List<MediaType> DEFAULT_DATASTRUCTURE_TYPES = singletonList(STRUCTURE_21);

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
        public @NonNull <V> FileParser<V> andThen(@NonNull IOFunction<? super T, ? extends V> after) {
            return new UnsupportedParser<>(mediaType);
        }
    }

    //    @MightBePromoted
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> FileParser<T> withCharset(Xml.Parser<T> parser, Optional<Charset> charset) {
        return charset.map(parser::asFileParser).orElse(parser);
    }
}
