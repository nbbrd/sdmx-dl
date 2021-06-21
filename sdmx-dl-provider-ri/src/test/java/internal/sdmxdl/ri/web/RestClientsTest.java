package internal.sdmxdl.ri.web;

import org.junit.Test;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;

import static org.assertj.core.api.Assertions.assertThat;

public class RestClientsTest {

    @Test
    public void testGetRestContext() {
        SdmxWebSource webSource = SdmxWebSource
                .builder()
                .name("abc")
                .driver("xyz")
                .endpointOf("http://localhost")
                .build();

        SdmxWebContext webContext = SdmxWebContext
                .builder()
                .build();

        assertThat(RestClients.getRestContext(webSource, webContext).getUserAgent())
                .startsWith("sdmx-dl/");

        String previous = System.setProperty(RestClients.HTTP_AGENT, "hello world");
        try {
            assertThat(RestClients.getRestContext(webSource, webContext).getUserAgent())
                    .startsWith("hello world");
        } finally {
            if (previous != null)
                System.setProperty(RestClients.HTTP_AGENT, previous);
        }
    }
}
