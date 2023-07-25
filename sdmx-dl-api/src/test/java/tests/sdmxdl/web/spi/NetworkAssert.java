package tests.sdmxdl.web.spi;

import lombok.NonNull;
import sdmxdl.web.spi.Network;

import static org.assertj.core.api.Assertions.assertThat;

@lombok.experimental.UtilityClass
public class NetworkAssert {

    public static void assertCompliance(@NonNull Network network) {
        assertThat(network.getProxySelector())
                .isNotNull();

        assertThat(network.getSSLFactory())
                .isNotNull()
                .satisfies(SSLFactoryAssert::assertCompliance);

        assertThat(network.getURLConnectionFactory())
                .isNotNull();
    }
}
