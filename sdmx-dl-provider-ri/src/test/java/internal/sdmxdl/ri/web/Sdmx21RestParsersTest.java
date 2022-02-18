package internal.sdmxdl.ri.web;

import internal.util.http.MediaType;
import nbbrd.io.FileParser;
import nbbrd.io.function.IOFunction;
import nbbrd.io.xml.Xml;
import org.junit.jupiter.api.Test;
import sdmxdl.DataRef;
import sdmxdl.DataSet;
import sdmxdl.DataStructure;
import sdmxdl.DataflowRef;
import sdmxdl.samples.ByteSource;
import sdmxdl.samples.RepoSamples;
import sdmxdl.samples.SdmxSource;
import sdmxdl.util.parser.ObsFactories;
import sdmxdl.xml.DataCursor;
import sdmxdl.xml.stream.SdmxXmlStreams;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static internal.sdmxdl.ri.web.RiHttpUtils.GENERIC_DATA_21_TYPE;
import static internal.sdmxdl.ri.web.RiHttpUtils.STRUCTURE_21_TYPE;
import static internal.sdmxdl.ri.web.Sdmx21RestParsers.*;
import static internal.util.http.MediaType.ANY_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static sdmxdl.LanguagePriorityList.ANY;

public class Sdmx21RestParsersTest {

    @Test
    public void testGetFlowTypes() {
        testType(DEFAULT_DATAFLOW_TYPES, Sdmx21RestParsers::getFlowTypes);
    }

    @Test
    public void testGetFlowParser() throws IOException {
        BiFunction<Sdmx21RestParsers, MediaType, FileParser<?>> extractor = (x, y) -> x.getFlowParser(y, ANY, RepoSamples.FLOW_REF);
        testParser(DEFAULT_DATAFLOW_TYPES, extractor);
        testContent(extractor, STRUCTURE_21_TYPE, SdmxSource.ECB_DATAFLOWS);
    }

    @Test
    public void testGetFlowsTypes() {
        testType(DEFAULT_DATAFLOW_TYPES, Sdmx21RestParsers::getFlowsTypes);
    }

    @Test
    public void testGetFlowsParser() throws IOException {
        BiFunction<Sdmx21RestParsers, MediaType, FileParser<?>> extractor = (x, y) -> x.getFlowsParser(y, ANY);
        testParser(DEFAULT_DATAFLOW_TYPES, extractor);
        testContent(extractor, STRUCTURE_21_TYPE, SdmxSource.ECB_DATAFLOWS);
    }

    @Test
    public void testGetStructureTypes() {
        testType(DEFAULT_DATASTRUCTURE_TYPES, Sdmx21RestParsers::getStructureTypes);
    }

    @Test
    public void testGetStructureParser() throws IOException {
        BiFunction<Sdmx21RestParsers, MediaType, FileParser<?>> extractor = (x, y) -> x.getStructureParser(y, ANY, RepoSamples.STRUCT_REF);
        testParser(DEFAULT_DATASTRUCTURE_TYPES, extractor);
        testContent(extractor, STRUCTURE_21_TYPE, SdmxSource.ECB_DATA_STRUCTURE);
    }

    @Test
    public void testGetDataTypes() {
        testType(DEFAULT_DATA_TYPES, Sdmx21RestParsers::getDataTypes);
    }

    @Test
    public void testGetDataParser() throws IOException {
        DataStructure dataStructure = SdmxXmlStreams.struct21(ANY).andThen(list -> list.get(0)).parseStream(SdmxSource.ECB_DATA_STRUCTURE::openStream);
        BiFunction<Sdmx21RestParsers, MediaType, FileParser<?>> extractor = (x, y) -> x.getDataParser(y, dataStructure, ObsFactories.SDMX21).andThen(IOFunction.unchecked(Sdmx21RestParsersTest::toDataSet));
        testParser(DEFAULT_DATA_TYPES, extractor);
        testContent(extractor, GENERIC_DATA_21_TYPE, SdmxSource.ECB_DATA);
    }

    private static DataSet toDataSet(DataCursor cursor) throws IOException {
        try {
            return cursor.toStream().collect(DataSet.toDataSet(DataRef.of(DataflowRef.parse("abc"))));
        } finally {
            cursor.close();
        }
    }

    private void testType(List<MediaType> defaultTypes, Function<Sdmx21RestParsers, List<MediaType>> extractor) {
        assertThat(new Sdmx21RestParsers())
                .extracting(extractor, LIST)
                .containsExactlyElementsOf(defaultTypes);
    }

    private void testParser(List<MediaType> defaultTypes, BiFunction<Sdmx21RestParsers, MediaType, FileParser<?>> extractor) {
        assertThat(new Sdmx21RestParsers())
                .extracting(x -> extractor.apply(x, ANY_TYPE))
                .isInstanceOf(Sdmx21RestParsers.UnsupportedParser.class);

        for (MediaType mediaType : defaultTypes) {
            assertThat(new Sdmx21RestParsers())
                    .extracting(x -> extractor.apply(x, mediaType))
                    .isNotInstanceOf(Sdmx21RestParsers.UnsupportedParser.class)
                    .isInstanceOf(Xml.Parser.class);

            assertThat(new Sdmx21RestParsers())
                    .extracting(x -> extractor.apply(x, mediaType.withoutParameters()))
                    .isNotInstanceOf(Sdmx21RestParsers.UnsupportedParser.class)
                    .isInstanceOf(Xml.Parser.class);

            assertThat(new Sdmx21RestParsers())
                    .extracting(x -> extractor.apply(x, mediaType.withCharset(StandardCharsets.US_ASCII)))
                    .isNotInstanceOf(Xml.Parser.class);
        }
    }

    private void testContent(BiFunction<Sdmx21RestParsers, MediaType, FileParser<?>> extractor, MediaType mediaType, ByteSource sample) throws IOException {
        Sdmx21RestParsers x = new Sdmx21RestParsers();
        assertThat(extractor.apply(x, mediaType).parseStream(sample::openStream))
                .isEqualTo(extractor.apply(x, mediaType.withCharset(StandardCharsets.UTF_8)).parseStream(sample::openStream));
    }
}
