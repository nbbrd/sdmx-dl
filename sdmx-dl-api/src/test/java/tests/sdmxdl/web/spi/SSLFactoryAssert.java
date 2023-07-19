package tests.sdmxdl.web.spi;

import lombok.NonNull;
import sdmxdl.web.spi.SSLFactory;

import static org.assertj.core.api.Assertions.assertThat;

@lombok.experimental.UtilityClass
public class SSLFactoryAssert {

    public static void assertCompliance(@NonNull SSLFactory x) {
        assertThat(x.getSSLSocketFactory())
                .isNotNull();

        assertThat(x.getHostnameVerifier())
                .isNotNull();
    }
}
