package sdmxdl.format.csv;

import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import org.junit.jupiter.api.Test;
import sdmxdl.FlowRef;

import static org.assertj.core.api.Assertions.assertThat;

public class SdmxCsvFieldsTest {

    @Test
    public void testParser() {
        Parser<FlowRef> parser = SdmxCsvFields.getDataflowRefParser();

        assertThat(parser.parse("ECB:EXR(1.0)"))
                .isEqualTo(FlowRef.of("ECB", "EXR", "1.0"));

        assertThat(parser.parse("all:EXR(latest)"))
                .isEqualTo(FlowRef.of(null, "EXR", null));

        assertThat(parser.parse(":()"))
                .isEqualTo(FlowRef.of(null, "", null));

        assertThat(parser.parse(null)).isNull();
        assertThat(parser.parse("all:EXR(latest")).isNull();
        assertThat(parser.parse("all:EXRlatest)")).isNull();
        assertThat(parser.parse("allEXR(latest)")).isNull();
        assertThat(parser.parse("ECB,EXR,1.0")).isNull();
        assertThat(parser.parse("EXR")).isNull();
        assertThat(parser.parse("")).isNull();
    }

    @Test
    public void testCombinedParser() {
        Parser<FlowRef> parser = SdmxCsvFields.getDataflowRefParser().orElse(Parser.of(FlowRef::parse));

        assertThat(parser.parse("ECB:EXR(1.0)"))
                .isEqualTo(FlowRef.of("ECB", "EXR", "1.0"));

        assertThat(parser.parse("ECB,EXR,1.0"))
                .isEqualTo(FlowRef.of("ECB", "EXR", "1.0"));

        assertThat(parser.parse("EXR"))
                .isEqualTo(FlowRef.of("all", "EXR", "latest"));
    }

    @Test
    public void testFormatter() {
        Formatter<FlowRef> formatter = SdmxCsvFields.getDataflowRefFormatter();

        assertThat(formatter.format(FlowRef.of("ECB", "EXR", "1.0")))
                .isEqualTo("ECB:EXR(1.0)");

        assertThat(formatter.format(FlowRef.of(null, "EXR", null)))
                .isEqualTo("all:EXR(latest)");

        assertThat(formatter.format(null)).isNull();
    }
}
