package internal.sdmxdl.ri.web;

import internal.util.rest.MediaType;
import nbbrd.io.FileParser;
import org.junit.Test;
import sdmxdl.samples.RepoSamples;
import sdmxdl.util.parser.ObsFactories;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static internal.sdmxdl.ri.web.Sdmx21RestParsers.*;
import static internal.util.rest.MediaType.ANY_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static sdmxdl.LanguagePriorityList.ANY;

public class Sdmx21RestParsersTest {

    @Test
    public void testGetFlowTypes() {
        testType(SdmxResourceType.DATAFLOW, DEFAULT_DATAFLOW_TYPES, Sdmx21RestParsers::getFlowTypes);
    }

    @Test
    public void testGetFlowParser() {
        testParser(SdmxResourceType.DATAFLOW, DEFAULT_DATAFLOW_TYPES, (x, y) -> x.getFlowParser(y, ANY, RepoSamples.GOOD_FLOW_REF));
    }

    @Test
    public void testGetFlowsTypes() {
        testType(SdmxResourceType.DATAFLOW, DEFAULT_DATAFLOW_TYPES, Sdmx21RestParsers::getFlowsTypes);
    }

    @Test
    public void testGetFlowsParser() {
        testParser(SdmxResourceType.DATAFLOW, DEFAULT_DATAFLOW_TYPES, (x, y) -> x.getFlowsParser(y, ANY));
    }

    @Test
    public void testGetStructureTypes() {
        testType(SdmxResourceType.DATASTRUCTURE, DEFAULT_DATASTRUCTURE_TYPES, Sdmx21RestParsers::getStructureTypes);
    }

    @Test
    public void testGetStructureParser() {
        testParser(SdmxResourceType.DATASTRUCTURE, DEFAULT_DATASTRUCTURE_TYPES, (x, y) -> x.getStructureParser(y, ANY, RepoSamples.GOOD_STRUCT_REF));
    }

    @Test
    public void testGetDataTypes() {
        testType(SdmxResourceType.DATA, DEFAULT_DATA_TYPES, Sdmx21RestParsers::getDataTypes);
    }

    @Test
    public void testGetDataParser() {
        testParser(SdmxResourceType.DATA, DEFAULT_DATA_TYPES, (x, y) -> x.getDataParser(y, RepoSamples.STRUCT, ObsFactories.SDMX21));
    }

    private void testType(SdmxResourceType target, List<MediaType> defaultTypes, Function<Sdmx21RestParsers, List<MediaType>> extractor) {
        assertThat(Sdmx21RestParsers.builder().build())
                .extracting(extractor, LIST)
                .containsExactlyElementsOf(defaultTypes);

        for (SdmxResourceType resource : SdmxResourceType.values()) {
            if (resource.equals(target)) {
                assertThat(Sdmx21RestParsers.builder().customType(resource, ANY_TYPE).build())
                        .extracting(extractor, LIST)
                        .containsExactly(ANY_TYPE);
            } else {
                assertThat(Sdmx21RestParsers.builder().customType(resource, ANY_TYPE).build())
                        .extracting(extractor, LIST)
                        .containsExactlyElementsOf(defaultTypes);
            }
            assertThat(Sdmx21RestParsers.builder().customType(resource, null).build())
                    .extracting(extractor, LIST)
                    .containsExactlyElementsOf(defaultTypes);
        }
    }

    private void testParser(SdmxResourceType target, List<MediaType> defaultTypes, BiFunction<Sdmx21RestParsers, MediaType, FileParser<?>> extractor) {
        assertThat(builder().build())
                .extracting(x -> extractor.apply(x, ANY_TYPE))
                .isInstanceOf(Sdmx21RestParsers.UnsupportedParser.class);

        for (MediaType mediaType : defaultTypes) {
            assertThat(builder().build())
                    .extracting(x -> extractor.apply(x, mediaType))
                    .isNotInstanceOf(Sdmx21RestParsers.UnsupportedParser.class);

            for (SdmxResourceType resource : SdmxResourceType.values()) {
                if (resource.equals(target)) {
                    assertThat(builder().customType(resource, mediaType).build())
                            .extracting(x -> extractor.apply(x, ANY_TYPE))
                            .isNotInstanceOf(Sdmx21RestParsers.UnsupportedParser.class);
                } else {
                    assertThat(builder().customType(resource, mediaType).build())
                            .extracting(x -> extractor.apply(x, ANY_TYPE))
                            .isInstanceOf(Sdmx21RestParsers.UnsupportedParser.class);
                }
            }
        }
    }
}
