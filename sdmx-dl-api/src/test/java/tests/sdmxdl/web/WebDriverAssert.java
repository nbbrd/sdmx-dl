package tests.sdmxdl.web;

import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import static org.assertj.core.api.Assertions.*;

@lombok.experimental.UtilityClass
public class WebDriverAssert {

    public WebContext noOpWebContext() {
        return WebContext.builder().build();
    }

    @SuppressWarnings("null")
    public void assertCompliance(WebDriver d) {
        SdmxWebSource validSource = SdmxWebSource
                .builder()
                .id("valid")
                .driver(d.getName())
                .dialect("azerty")
                .endpointOf("http://localhost")
                .build();

        SdmxWebSource invalidSource = validSource.toBuilder().driver("").build();

        WebContext context = WebDriverAssert.noOpWebContext();

        assertThat(d.getName()).isNotBlank();

        assertThatNullPointerException().isThrownBy(() -> d.connect(null, context));
        assertThatNullPointerException().isThrownBy(() -> d.connect(validSource, null));

        assertThatIllegalArgumentException().isThrownBy(() -> d.connect(invalidSource, context));

        assertThat(d.getDefaultSources()).allSatisfy(o -> checkSource(o, d));

        assertThat(d.getClass()).isFinal();
    }

    private void checkSource(SdmxWebSource o, WebDriver d) {
        assertThat(o.getId()).isNotBlank();
        assertThat(o.getProperties()).isNotNull();
        assertThat(o.getDriver()).isEqualTo(d.getName());
        assertThat(o.getProperties().keySet()).isSubsetOf(d.getSupportedProperties());
    }
}
