package sdmxdl.provider.ri.http;

import nbbrd.io.http.urlconnection.UrlConnectionHttpClient;
import org.junit.jupiter.api.Test;
import sdmxdl.provider.web.DriverProperties;
import sdmxdl.web.WebSource;
import tests.sdmxdl.web.spi.DriverAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.provider.ri.http.DefaultHttpFactory.newHttpClient;

public class DefaultHttpFactoryTest {

    WebSource source = WebSource
            .builder()
            .id("abc")
            .driver("xyz")
            .endpointOf("http://localhost")
            .build();

    @Test
    public void testUserAgent() {
        assertThat(((UrlConnectionHttpClient) newHttpClient(source, DriverAssert.noOpWebContext())).getUserAgent())
                .startsWith("sdmx-dl/");

        assertThat(((UrlConnectionHttpClient) newHttpClient(source.toBuilder().property(DriverProperties.USER_AGENT_PROPERTY.getKey(), "hello world").build(), DriverAssert.noOpWebContext())).getUserAgent())
                .startsWith("hello world");
    }
}
