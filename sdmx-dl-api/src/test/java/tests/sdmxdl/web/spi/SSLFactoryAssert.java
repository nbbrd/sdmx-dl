package tests.sdmxdl.web.spi;

import lombok.NonNull;
import sdmxdl.web.spi.SSLFactory;

import static org.assertj.core.api.Assertions.assertThat;

@lombok.experimental.UtilityClass
public class SSLFactoryAssert {

    public static void assertCompliance(@NonNull SSLFactory sslFactory) {
        assertThat(sslFactory.getSSLSocketFactory())
                .isNotNull();

        assertThat(sslFactory.getHostnameVerifier())
                .isNotNull();
    }
}
