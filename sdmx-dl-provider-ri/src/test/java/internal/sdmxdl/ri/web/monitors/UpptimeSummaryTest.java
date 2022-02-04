package internal.sdmxdl.ri.web.monitors;

import nbbrd.io.text.TextResource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

public class UpptimeSummaryTest {

    @Test
    public void testParseAll() throws IOException {
        try (Reader reader = TextResource.getResourceAsBufferedReader(UpptimeTest.class, "summary.json", StandardCharsets.UTF_8).get()) {
            assertThat(UpptimeSummary.parseAll(reader))
                    .contains(new UpptimeSummary("ABS", "up", "100.00%", 4674), atIndex(0))
                    .contains(new UpptimeSummary("ILO", "down", "20.97%", 14989), atIndex(1))
                    .hasSize(2);
        }
    }
}
