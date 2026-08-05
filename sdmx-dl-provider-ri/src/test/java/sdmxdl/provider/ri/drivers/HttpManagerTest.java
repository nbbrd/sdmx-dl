package sdmxdl.provider.ri.drivers;

import nbbrd.io.text.BaseProperty;
import org.junit.jupiter.api.Test;
import sdmxdl.provider.ri.http.DumpingDecoration;
import sdmxdl.provider.ri.http.HttpManager;
import sdmxdl.provider.ri.http.RetryDecoration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class HttpManagerTest {

    @Test
    public void testFactory() {
        assertThatNullPointerException()
                .isThrownBy(() -> HttpManager.getHttpFactory().createHttpClient(null, null));
    }

    @Test
    public void testDefaultFactory() {
        assertThat(HttpManager.getHttpFactory().getFactoryName())
                .isEqualTo("UrlConnectionHttpClientFactory with Lazy with Authenticating with Cookie with Redirect with Retry with Rate-limiting with Throwing with Dumping with Metrics");

        assertThat(HttpManager.getHttpFactory().getHttpClientProperties())
                .hasSize(9)
                .extracting(BaseProperty::getKey)
                .contains(DumpingDecoration.DUMP_FOLDER_PROPERTY.getKey())
                .contains(RetryDecoration.MAX_RETRIES_PROPERTY.getKey());
    }
}