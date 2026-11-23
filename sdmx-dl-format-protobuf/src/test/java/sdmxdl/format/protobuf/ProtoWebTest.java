package sdmxdl.format.protobuf;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import sdmxdl.web.WebSource;

public class ProtoWebTest {

    @Test
    public void testSources() {
        WebSource min = WebSource.builder()
                .id("ESTAT")
                .driver("abc")
                .endpointOf("http://endpoint")
                .build();
        assertThat(ProtoWeb.fromWebSource(min))
                .extracting(ProtoWeb::toWebSource)
                .isEqualTo(min);

        WebSource max = min.toBuilder()
                .name("en", "hello")
                .property("key", "value")
                .alias("EUROSTAT")
                .websiteOf("http://website")
                .monitorOf("monitor:ESTAT")
                .monitorWebsiteOf("http://monitorwebsite")
                .iconOf("sdmx-dl:icon/ESTAT")
                .build();
        assertThat(ProtoWeb.fromWebSource(max))
                .extracting(ProtoWeb::toWebSource)
                .isEqualTo(max);
    }
}
