package sdmxdl.provider.px.drivers;

import org.junit.jupiter.api.Test;
import sdmxdl.Confidentiality;
import sdmxdl.web.WebSources;
import sdmxdl.web.WebSource;

import java.io.IOException;
import java.io.InputStream;

import static nbbrd.io.Resource.newInputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static sdmxdl.provider.px.drivers.PxWebDriver.PX_PXWEB;

public class PxWebSourcesFormatTest {

    @Test
    public void test() throws IOException {
        try (InputStream stream = newInputStream(PxWebDriver.class, "api.json")) {
            assertThat(PxWebSourcesFormat.INSTANCE.parseStream(stream))
                    .extracting(WebSources::getSources, list(WebSource.class))
                    .hasSize(30)
                    .element(0)
                    .isEqualTo(WebSource
                            .builder()
                            .id("SCB")
                            .name("en", "Statistics Sweden")
                            .driver(PX_PXWEB)
                            .confidentiality(Confidentiality.PUBLIC)
                            .endpointOf("https://api.scb.se/OV0104/_VERSION_/doris/_LANG_")
                            .propertyOf("sdmxdl.driver.versions", "v1")
                            .propertyOf("sdmxdl.driver.languages", "en,sv")
                            .build());
        }
    }
}
