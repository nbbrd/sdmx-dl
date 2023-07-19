package tests.sdmxdl.web.spi;

import lombok.NonNull;
import sdmxdl.web.spi.Network;

import static org.assertj.core.api.Assertions.assertThat;

@lombok.experimental.UtilityClass
public class NetworkAssert {

    public static void assertCompliance(@NonNull Network x) {
        assertThat(x.getProxySelector())
                .isNotNull();

        assertThat(x.getSSLFactory())
                .isNotNull()
                .satisfies(SSLFactoryAssert::assertCompliance);

        assertThat(x.getURLConnectionFactory())
                .isNotNull();
    }
}
