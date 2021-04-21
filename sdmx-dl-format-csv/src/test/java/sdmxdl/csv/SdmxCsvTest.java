package sdmxdl.csv;

import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import org.junit.Test;
import sdmxdl.DataflowRef;

import static org.assertj.core.api.Assertions.assertThat;

public class SdmxCsvTest {

    @Test
    public void testParser() {
        Parser<DataflowRef> parser = SdmxCsv.getDataflowRefParser();

        assertThat(parser.parse("ECB:EXR(1.0)"))
                .isEqualTo(DataflowRef.of("ECB", "EXR", "1.0"));

        assertThat(parser.parse("all:EXR(latest)"))
                .isEqualTo(DataflowRef.of(null, "EXR", null));

        assertThat(parser.parse(null)).isNull();
        assertThat(parser.parse("all:EXR(latest")).isNull();
        assertThat(parser.parse("all:EXRlatest)")).isNull();
        assertThat(parser.parse("allEXR(latest)")).isNull();
        assertThat(parser.parse("ECB,EXR,1.0")).isNull();
        assertThat(parser.parse("EXR")).isNull();
    }

    @Test
    public void testCombinedParser() {
        Parser<DataflowRef> parser = SdmxCsv.getDataflowRefParser().orElse(DataflowRef::parse);

        assertThat(parser.parse("ECB:EXR(1.0)"))
                .isEqualTo(DataflowRef.of("ECB", "EXR", "1.0"));

        assertThat(parser.parse("ECB,EXR,1.0"))
                .isEqualTo(DataflowRef.of("ECB", "EXR", "1.0"));

        assertThat(parser.parse("EXR"))
                .isEqualTo(DataflowRef.of("all", "EXR", "latest"));
    }

    @Test
    public void testFormatter() {
        Formatter<DataflowRef> formatter = SdmxCsv.getDataflowRefFormatter();

        assertThat(formatter.format(DataflowRef.of("ECB", "EXR", "1.0")))
                .isEqualTo("ECB:EXR(1.0)");

        assertThat(formatter.format(DataflowRef.of(null, "EXR", null)))
                .isEqualTo("all:EXR(latest)");

        assertThat(formatter.format(null)).isNull();
    }
}
