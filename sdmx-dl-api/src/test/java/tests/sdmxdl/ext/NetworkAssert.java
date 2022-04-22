package tests.sdmxdl.ext;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.web.Network;
import tests.sdmxdl.api.TckUtil;

@lombok.experimental.UtilityClass
public class NetworkAssert {

    public void assertCompliance(Network network) {
        TckUtil.run(s -> assertCompliance(s, network));
    }

    public void assertCompliance(SoftAssertions s, Network network) {
        s.assertThat(network.getHostnameVerifier()).isNotNull();
        s.assertThat(network.getSSLSocketFactory()).isNotNull();
        s.assertThat(network.getProxySelector()).isNotNull();
    }
}
