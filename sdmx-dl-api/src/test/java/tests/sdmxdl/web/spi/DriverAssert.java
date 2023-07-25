package tests.sdmxdl.web.spi;

import lombok.NonNull;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.Languages.ANY;
import static sdmxdl.web.spi.Driver.DRIVER_PROPERTY_PREFIX;
import static tests.sdmxdl.api.TckUtil.startingWith;

@lombok.experimental.UtilityClass
public class DriverAssert {

    public WebContext noOpWebContext() {
        return WebContext.builder().build();
    }

    @SuppressWarnings("null")
    public void assertCompliance(@NonNull Driver driver) {
        assertThat(driver.getDriverId())
//                .containsPattern(SCREAMING_SNAKE_CASE)
                .isNotBlank();

        assertThat(driver.getDriverProperties())
                .are(startingWith(DRIVER_PROPERTY_PREFIX))
                .doesNotHaveDuplicates();

        SdmxWebSource validSource = SdmxWebSource
                .builder()
                .id("valid")
                .driver(driver.getDriverId())
                .endpointOf("http://localhost")
                .build();

        SdmxWebSource invalidSource = validSource.toBuilder().driver("").build();

        WebContext context = DriverAssert.noOpWebContext();

        assertThat(driver.getDriverId()).isNotBlank();

        assertThatNullPointerException().isThrownBy(() -> driver.connect(null, ANY, context));
        assertThatNullPointerException().isThrownBy(() -> driver.connect(validSource, null, context));
        assertThatNullPointerException().isThrownBy(() -> driver.connect(validSource, ANY, null));

        assertThatIllegalArgumentException().isThrownBy(() -> driver.connect(invalidSource, ANY, context));

        assertThat(driver.getDefaultSources()).allSatisfy(o -> checkSource(o, driver));

        assertThat(driver.getClass()).isFinal();
    }

    private void checkSource(SdmxWebSource o, Driver d) {
        assertThat(o.getId()).isNotBlank();
        assertThat(o.getProperties()).isNotNull();
        assertThat(o.getDriver()).isEqualTo(d.getDriverId());
        assertThat(o.getProperties().keySet()).isSubsetOf(d.getDriverProperties());
    }
}
