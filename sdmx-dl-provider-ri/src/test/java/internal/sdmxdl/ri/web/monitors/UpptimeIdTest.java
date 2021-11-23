package internal.sdmxdl.ri.web.monitors;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class UpptimeIdTest {

    @Test
    public void testToString() {
        assertThat(new UpptimeId("nbbrd", "sdmx-upptime", "ECB").toString())
                .isEqualTo("upptime:/nbbrd/sdmx-upptime/ECB");
    }

    @Test
    public void toSummaryURL() throws MalformedURLException {
        assertThat(new UpptimeId("", "", "").toSummaryURL())
                .isEqualTo(new URL("https://raw.githubusercontent.com///master/history/summary.json"));

        assertThat(new UpptimeId("nbbrd", "sdmx-upptime", "ECB").toSummaryURL())
                .isEqualTo(new URL("https://raw.githubusercontent.com/nbbrd/sdmx-upptime/master/history/summary.json"));
    }

    @Test
    public void parse() {
        assertThat(UpptimeId.parse(URI.create("upptime:/nbbrd/sdmx-upptime/ECB")))
                .isEqualTo(new UpptimeId("nbbrd", "sdmx-upptime", "ECB"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> UpptimeId.parse(URI.create("abc:/nbbrd/sdmx-upptime/ECB")))
                .withMessageContaining("Invalid scheme");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> UpptimeId.parse(URI.create("upptime:nbbrd/sdmx-upptime/ECB/")))
                .withMessageContaining("Missing path");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> UpptimeId.parse(URI.create("upptime:/")))
                .withMessageContaining("Invalid path");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> UpptimeId.parse(URI.create("upptime:/nbbrd")))
                .withMessageContaining("Invalid path");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> UpptimeId.parse(URI.create("upptime:/nbbrd/sdmx-upptime")))
                .withMessageContaining("Invalid path");
    }
}