package tests.sdmxdl.web;

import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.LanguagePriorityList.ANY;

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
                .driver(d.getId())
                .endpointOf("http://localhost")
                .build();

        SdmxWebSource invalidSource = validSource.toBuilder().driver("").build();

        WebContext context = WebDriverAssert.noOpWebContext();

        assertThat(d.getId()).isNotBlank();

        assertThatNullPointerException().isThrownBy(() -> d.connect(null, ANY, context));
        assertThatNullPointerException().isThrownBy(() -> d.connect(validSource, null, context));
        assertThatNullPointerException().isThrownBy(() -> d.connect(validSource, ANY, null));

        assertThatIllegalArgumentException().isThrownBy(() -> d.connect(invalidSource, ANY, context));

        assertThat(d.getDefaultSources()).allSatisfy(o -> checkSource(o, d));

        assertThat(d.getClass()).isFinal();
    }

    private void checkSource(SdmxWebSource o, WebDriver d) {
        assertThat(o.getId()).isNotBlank();
        assertThat(o.getProperties()).isNotNull();
        assertThat(o.getDriver()).isEqualTo(d.getId());
        assertThat(o.getProperties().keySet()).isSubsetOf(d.getSupportedProperties());
    }
}
