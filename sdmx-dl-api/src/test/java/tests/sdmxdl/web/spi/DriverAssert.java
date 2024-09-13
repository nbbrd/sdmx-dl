package tests.sdmxdl.web.spi;

import internal.sdmxdl.web.spi.DriverLoader;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import sdmxdl.Confidentiality;
import sdmxdl.Options;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;
import tests.sdmxdl.api.ExtensionPoint;
import tests.sdmxdl.api.TckUtil;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.Languages.ANY;

@lombok.experimental.UtilityClass
public class DriverAssert {

    public WebContext noOpWebContext() {
        return WebContext.builder().build();
    }

    @MightBeGenerated
    private static final ExtensionPoint<Driver> EXTENSION_POINT = ExtensionPoint
            .<Driver>builder()
            .id(Driver::getDriverId)
            .idPattern(DriverLoader.ID_PATTERN)
            .rank(Driver::getDriverRank)
            .rankLowerBound(Driver.UNKNOWN_DRIVER_RANK)
            .properties(Driver::getDriverProperties)
            .propertiesPrefix(Driver.DRIVER_PROPERTY_PREFIX)
            .build();

    @SuppressWarnings("null")
    public void assertCompliance(@NonNull Driver driver) {
        TckUtil.run(s -> EXTENSION_POINT.assertCompliance(s, driver));

        WebSource validSource = WebSource
                .builder()
                .id("valid")
                .driver(driver.getDriverId())
                .endpointOf("http://localhost")
                .build();

        WebSource invalidSource = validSource.toBuilder().driver("").build();

        WebContext context = DriverAssert.noOpWebContext();

        assertThatNullPointerException().isThrownBy(() -> driver.connect(null, Options.of(ANY), context));
        assertThatNullPointerException().isThrownBy(() -> driver.connect(validSource, null, context));
        assertThatNullPointerException().isThrownBy(() -> driver.connect(validSource, Options.of(ANY), null));

        assertThatIllegalArgumentException().isThrownBy(() -> driver.connect(invalidSource, Options.of(ANY), context));

        assertThat(driver.getDefaultSources()).allSatisfy(o -> checkSource(o, driver));
    }

    private void checkSource(WebSource o, Driver d) {
        assertThat(o.getId()).isNotBlank();
        assertThat(o.getProperties()).isNotNull();
        assertThat(o.getDriver()).isEqualTo(d.getDriverId());
        assertThat(o.getProperties().keySet()).isSubsetOf(d.getDriverProperties());
        assertThat(o.getConfidentiality()).isEqualTo(Confidentiality.PUBLIC);
    }
}
