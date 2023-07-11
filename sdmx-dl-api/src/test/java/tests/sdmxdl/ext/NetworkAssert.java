package tests.sdmxdl.ext;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.web.spi.Network;
import tests.sdmxdl.api.TckUtil;

@lombok.experimental.UtilityClass
public class NetworkAssert {

    public void assertCompliance(Network network) {
        TckUtil.run(s -> assertCompliance(s, network));
    }

    public void assertCompliance(SoftAssertions s, Network network) {
        s.assertThat(network.getProxySelector()).isNotNull();
        s.assertThat(network.getSSLFactory()).isNotNull();
        s.assertThat(network.getURLConnectionFactory()).isNotNull();
    }
}
