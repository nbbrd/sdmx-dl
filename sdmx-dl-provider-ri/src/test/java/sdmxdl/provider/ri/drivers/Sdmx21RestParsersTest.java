package sdmxdl.provider.ri.drivers;

import nbbrd.io.FileParser;
import nbbrd.io.net.MediaType;
import nbbrd.io.xml.Xml;
import org.junit.jupiter.api.Test;
import sdmxdl.*;
import sdmxdl.format.DataCursor;
import sdmxdl.format.ObsParser;
import sdmxdl.format.xml.SdmxXmlStreams;
import sdmxdl.format.xml.XmlMediaTypes;
import tests.sdmxdl.api.ByteSource;
import tests.sdmxdl.api.RepoSamples;
import tests.sdmxdl.format.xml.SdmxXmlSources;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static nbbrd.io.net.MediaType.ANY_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static sdmxdl.Languages.ANY;
import static sdmxdl.provider.ri.drivers.Sdmx21RestParsers.*;

public class Sdmx21RestParsersTest {

    @Test
    public void testGetFlowsTypes() {
        testType(DEFAULT_DATAFLOW_TYPES, Sdmx21RestParsers::getFlowsTypes);
    }

    @Test
    public void testGetFlowsParser() throws IOException {
        BiFunction<Sdmx21RestParsers, MediaType, FileParser<?>> extractor = (x, y) -> x.getFlowsParser(y, ANY);
        testParser(DEFAULT_DATAFLOW_TYPES, extractor);
        testContent(extractor, XmlMediaTypes.STRUCTURE_21, SdmxXmlSources.ECB_DATAFLOWS);
    }

    @Test
    public void testGetStructureTypes() {
        testType(DEFAULT_DATASTRUCTURE_TYPES, Sdmx21RestParsers::getStructureTypes);
    }

    @Test
    public void testGetStructureParser() throws IOException {
        BiFunction<Sdmx21RestParsers, MediaType, FileParser<?>> extractor = (x, y) -> x.getStructureParser(y, ANY, RepoSamples.STRUCT_REF);
        testParser(DEFAULT_DATASTRUCTURE_TYPES, extractor);
        testContent(extractor, XmlMediaTypes.STRUCTURE_21, SdmxXmlSources.ECB_DATA_STRUCTURE);
    }

    @Test
    public void testGetDataTypes() {
        testType(DEFAULT_DATA_TYPES, Sdmx21RestParsers::getDataTypes);
    }

    @Test
    public void testGetDataParser() throws IOException {
        Structure structure = SdmxXmlStreams.struct21(ANY).andThen(list -> list.get(0)).parseStream(SdmxXmlSources.ECB_DATA_STRUCTURE::openStream);
        BiFunction<Sdmx21RestParsers, MediaType, FileParser<?>> extractor = (x, y) -> x.getDataParser(y, structure, ObsParser::newDefault).andThen(Sdmx21RestParsersTest::toDataSet);
        testParser(DEFAULT_DATA_TYPES, extractor);
        testContent(extractor, XmlMediaTypes.GENERIC_DATA_21, SdmxXmlSources.ECB_DATA);
    }

    private static DataSet toDataSet(DataCursor cursor) throws IOException {
        try (Stream<Series> stream = cursor.asCloseableStream()) {
            return stream.collect(DataSet.toDataSet(FlowRef.parse("abc"), Query.ALL));
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    private void testType(List<MediaType> defaultTypes, Function<Sdmx21RestParsers, List<MediaType>> extractor) {
        assertThat(Sdmx21RestParsers.DEFAULT)
                .extracting(extractor, LIST)
                .containsExactlyElementsOf(defaultTypes);
    }

    private void testParser(List<MediaType> defaultTypes, BiFunction<Sdmx21RestParsers, MediaType, FileParser<?>> extractor) {
        assertThat(Sdmx21RestParsers.DEFAULT)
                .extracting(x -> extractor.apply(x, ANY_TYPE))
                .isInstanceOf(Sdmx21RestParsers.UnsupportedParser.class);

        for (MediaType mediaType : defaultTypes) {
            assertThat(Sdmx21RestParsers.DEFAULT)
                    .extracting(x -> extractor.apply(x, mediaType))
                    .isNotInstanceOf(Sdmx21RestParsers.UnsupportedParser.class)
                    .isInstanceOf(Xml.Parser.class);

            assertThat(Sdmx21RestParsers.DEFAULT)
                    .extracting(x -> extractor.apply(x, mediaType.withoutParameters()))
                    .isNotInstanceOf(Sdmx21RestParsers.UnsupportedParser.class)
                    .isInstanceOf(Xml.Parser.class);

            assertThat(Sdmx21RestParsers.DEFAULT)
                    .extracting(x -> extractor.apply(x, mediaType.withCharset(StandardCharsets.US_ASCII)))
                    .isNotInstanceOf(Xml.Parser.class);
        }
    }

    private void testContent(BiFunction<Sdmx21RestParsers, MediaType, FileParser<?>> extractor, MediaType mediaType, ByteSource sample) throws IOException {
        Sdmx21RestParsers x = Sdmx21RestParsers.DEFAULT;
        assertThat(extractor.apply(x, mediaType).parseStream(sample::openStream))
                .isEqualTo(extractor.apply(x, mediaType.withCharset(StandardCharsets.UTF_8)).parseStream(sample::openStream));
    }
}
