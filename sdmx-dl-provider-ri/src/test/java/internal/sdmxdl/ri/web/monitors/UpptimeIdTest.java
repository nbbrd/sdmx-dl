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
        assertThat(UpptimeId.builder().owner("nbbrd").repo("sdmx-upptime").site("ECB").build().toString())
                .isEqualTo("upptime:/nbbrd/sdmx-upptime/ECB");
    }

    @Test
    public void toSummaryURL() throws MalformedURLException {
        assertThat(UpptimeId.builder().owner("").repo("").site("").build().toSummaryURL())
                .isEqualTo(new URL("https://raw.githubusercontent.com///master/history/summary.json"));

        assertThat(UpptimeId.builder().owner("nbbrd").repo("sdmx-upptime").site("ECB").build().toSummaryURL())
                .isEqualTo(new URL("https://raw.githubusercontent.com/nbbrd/sdmx-upptime/master/history/summary.json"));
    }

    @Test
    public void parse() {
        assertThat(UpptimeId.parse(URI.create("upptime:/nbbrd/sdmx-upptime/ECB")))
                .isEqualTo(UpptimeId.builder().owner("nbbrd").repo("sdmx-upptime").site("ECB").build());

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