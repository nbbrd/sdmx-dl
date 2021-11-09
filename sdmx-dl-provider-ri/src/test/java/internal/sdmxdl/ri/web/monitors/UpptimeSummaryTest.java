package internal.sdmxdl.ri.web.monitors;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

public class UpptimeSummaryTest {

    @Test
    public void testParseAll() throws IOException {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(UpptimeTest.class.getResourceAsStream("summary.json")), StandardCharsets.UTF_8)) {
            assertThat(UpptimeSummary.parseAll(reader))
                    .contains(UpptimeSummary.of("ABS", "up", "100.00%", 4674), atIndex(0))
                    .contains(UpptimeSummary.of("ILO", "down", "20.97%", 14989), atIndex(1))
                    .hasSize(2);
        }
    }
}
