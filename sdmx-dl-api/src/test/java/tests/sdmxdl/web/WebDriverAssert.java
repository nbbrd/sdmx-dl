package tests.sdmxdl.web;

import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.Languages.ANY;

@lombok.experimental.UtilityClass
public class WebDriverAssert {

    public WebContext noOpWebContext() {
        return WebContext.builder().build();
    }

    @SuppressWarnings("null")
    public void assertCompliance(Driver d) {
        SdmxWebSource validSource = SdmxWebSource
                .builder()
                .id("valid")
                .driver(d.getDriverId())
                .endpointOf("http://localhost")
                .build();

        SdmxWebSource invalidSource = validSource.toBuilder().driver("").build();

        WebContext context = WebDriverAssert.noOpWebContext();

        assertThat(d.getDriverId()).isNotBlank();

        assertThatNullPointerException().isThrownBy(() -> d.connect(null, ANY, context));
        assertThatNullPointerException().isThrownBy(() -> d.connect(validSource, null, context));
        assertThatNullPointerException().isThrownBy(() -> d.connect(validSource, ANY, null));

        assertThatIllegalArgumentException().isThrownBy(() -> d.connect(invalidSource, ANY, context));

        assertThat(d.getDefaultSources()).allSatisfy(o -> checkSource(o, d));

        assertThat(d.getClass()).isFinal();
    }

    private void checkSource(SdmxWebSource o, Driver d) {
        assertThat(o.getId()).isNotBlank();
        assertThat(o.getProperties()).isNotNull();
        assertThat(o.getDriver()).isEqualTo(d.getDriverId());
        assertThat(o.getProperties().keySet()).isSubsetOf(d.getDriverProperties());
    }
}
