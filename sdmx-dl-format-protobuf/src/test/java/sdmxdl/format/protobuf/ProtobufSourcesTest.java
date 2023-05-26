package sdmxdl.format.protobuf;

import org.junit.jupiter.api.Test;
import sdmxdl.web.SdmxWebSource;

import static org.assertj.core.api.Assertions.assertThat;

public class ProtobufSourcesTest {

    @Test
    public void testSources() {
        SdmxWebSource min = SdmxWebSource
                .builder()
                .id("ESTAT")
                .driver("abc")
                .endpointOf("http://endpoint")
                .build();
        assertThat(ProtobufSources.fromWebSource(min))
                .extracting(ProtobufSources::toWebSource)
                .isEqualTo(min);

        SdmxWebSource max = min
                .toBuilder()
                .name("en", "hello")
                .dialect("OTHER")
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
