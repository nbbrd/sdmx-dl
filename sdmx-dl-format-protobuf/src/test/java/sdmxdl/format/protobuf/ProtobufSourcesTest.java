package sdmxdl.format.protobuf;

import org.junit.jupiter.api.Test;
import sdmxdl.web.WebSource;

import static org.assertj.core.api.Assertions.assertThat;

public class ProtobufSourcesTest {

    @Test
    public void testSources() {
        WebSource min = WebSource
                .builder()
                .id("ESTAT")
                .driver("abc")
                .endpointOf("http://endpoint")
                .build();
        assertThat(ProtobufSources.fromWebSource(min))
                .extracting(ProtobufSources::toWebSource)
                .isEqualTo(min);

        WebSource max = min
                .toBuilder()
                .name("en", "hello")
                .property("key", "value")
                .alias("EUROSTAT")
                .websiteOf("http://website")
                .monitorOf("monitor:ESTAT")
                .monitorWebsiteOf("http://monitorwebsite")
                .build();
        assertThat(ProtobufSources.fromWebSource(max))
                .extracting(ProtobufSources::toWebSource)
                .isEqualTo(max);
    }
}
