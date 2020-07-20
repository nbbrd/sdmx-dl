package sdmxdl.tck;

import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import static org.assertj.core.api.Assertions.*;

@lombok.experimental.UtilityClass
public class SdmxWebDriverAssert {

    @SuppressWarnings("null")
    public void assertCompliance(SdmxWebDriver d) {
        SdmxWebSource validSource = SdmxWebSource
                .builder()
                .name("valid")
                .driver(d.getName())
                .dialect("azerty")
                .endpointOf("http://localhost")
                .build();

        SdmxWebSource invalidSource = validSource.toBuilder().driver("").build();

        SdmxWebContext context = SdmxWebContext.builder().obsFactory(dsd -> null).build();

        assertThat(d.getName()).isNotBlank();

        assertThatNullPointerException().isThrownBy(() -> d.connect(null, context));
        assertThatNullPointerException().isThrownBy(() -> d.connect(validSource, null));

        assertThatIllegalArgumentException().isThrownBy(() -> d.connect(invalidSource, context));

        assertThat(d.getDefaultSources()).allSatisfy(o -> checkSource(o, d));

        assertThat(d.getClass()).isFinal();
    }

    private void checkSource(SdmxWebSource o, SdmxWebDriver d) {
        assertThat(o.getName()).isNotBlank();
        assertThat(o.getDescription()).isNotBlank();
        assertThat(o.getProperties()).isNotNull();
        assertThat(o.getDriver()).isEqualTo(d.getName());
        assertThat(o.getProperties().keySet()).isSubsetOf(d.getSupportedProperties());
    }
}
