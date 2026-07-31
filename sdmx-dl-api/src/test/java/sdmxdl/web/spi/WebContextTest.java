package sdmxdl.web.spi;

import org.junit.jupiter.api.Test;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.web.WebSource;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.sdmxdl.api.RepoSamples.BASIC_SOURCE;

/**
 * @author Philippe Charles
 */
public class WebContextTest {

    @Test
    public void testDefaults() {
        WebContext context = WebContext.builder().build();

        assertThat(context.getCaching()).isEqualTo(WebCaching.noOp());
        assertThat(context.getNetworking()).isEqualTo(Networking.getDefault());
        assertThat(context.getAuthenticators()).isEmpty();
        assertThat(context.getOnEvent()).isNull();
        assertThat(context.getOnError()).isNull();
    }

    @Test
    public void testGetEventListener() {
        WebSource source = BASIC_SOURCE;

        assertThat(WebContext.builder().build().getEventListener(source))
                .isNull();

        EventListener listener = (marker, message) -> {
        };
        assertThat(WebContext.builder().onEvent(x -> listener).build().getEventListener(source))
                .isSameAs(listener);
    }

    @Test
    public void testGetErrorListener() {
        WebSource source = BASIC_SOURCE;

        assertThat(WebContext.builder().build().getErrorListener(source))
                .isNull();

        ErrorListener listener = (marker, message, error) -> {
        };
        assertThat(WebContext.builder().onError(x -> listener).build().getErrorListener(source))
                .isSameAs(listener);
    }

    @Test
    public void testGetCachesAndNetwork() {
        WebContext context = WebContext.builder().build();

        assertThat(context.getDriverCache(BASIC_SOURCE)).isNotNull();
        assertThat(context.getMonitorCache(BASIC_SOURCE)).isNotNull();
        assertThat(context.getNetwork(BASIC_SOURCE)).isNotNull();
    }
}

