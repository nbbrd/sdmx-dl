package tests.sdmxdl.ext;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.ext.NetworkFactory;
import tests.sdmxdl.api.TckUtil;

@lombok.experimental.UtilityClass
public class NetworkFactoryAssert {

    public void assertCompliance(NetworkFactory factory) {
        TckUtil.run(s -> assertCompliance(s, factory));
    }

    public void assertCompliance(SoftAssertions s, NetworkFactory factory) {
        s.assertThat(factory.getHostnameVerifier()).isNotNull();
        s.assertThat(factory.getSslSocketFactory()).isNotNull();
        s.assertThat(factory.getProxySelector()).isNotNull();
    }
}
