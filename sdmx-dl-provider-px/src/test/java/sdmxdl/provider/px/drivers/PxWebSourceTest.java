package sdmxdl.provider.px.drivers;

import org.junit.jupiter.api.Test;
import sdmxdl.web.WebSource;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class PxWebSourceTest {

    @Test
    public void test() throws IOException {
        assertThat(PxWebSource.getParser().parseResource(PxWebSource.class, "api.json", UTF_8))
                .hasSize(30)
                .element(0)
                .isEqualTo(WebSource
                        .builder()
                        .id("SCB")
                        .name("en", "Statistics Sweden")
                        .driver("px:pxweb")
                        .endpointOf("https://api.scb.se/OV0104/_VERSION_/doris/_LANG_")
                        .propertyOf("sdmxdl.driver.versions", "v1")
                        .propertyOf("sdmxdl.driver.languages", "en,sv")
                        .build());
    }
}
