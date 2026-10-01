package internal.sdmxdl.script;

import static internal.sdmxdl.script.ScriptFormats.encodePathSegment;
import static internal.sdmxdl.script.ScriptFormats.formatPeriod;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sdmxdl.TimeInterval;

public class ScriptFormatsTest {

    @ParameterizedTest
    @ValueSource(strings = {"2020", "2020-02", "2020-01-02", "2020-01-01T13:00:00", "2020-01-01T00:00:01"})
    public void testFormatPeriod(String text) {
        assertThat(formatPeriod(TimeInterval.parseStart(text))).isEqualTo(text);
    }

    @Test
    public void testEncodePathSegment() {
        assertThat(encodePathSegment("ECB")).isEqualTo("ECB");
        assertThat(encodePathSegment("a b,c/d")).isEqualTo("a%20b%2Cc%2Fd");
        assertThat(encodePathSegment("é~-._")).isEqualTo("%C3%A9~-._");
    }
}
