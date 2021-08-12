package internal.util.rest;

import internal.util.http.HttpURLConnectionFactory;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class DefaultClientTest extends HttpRestClientTest {

    abstract protected HttpURLConnectionFactory getURLConnectionFactory();

    @Override
    protected HttpRest.Client getRestClient(HttpRest.Context context) {
        return new DefaultClient(context, getURLConnectionFactory());
    }

    @Test
    public void testToAcceptHeader() {
        assertThat(DefaultClient.toAcceptHeader(emptyList()))
                .isEqualTo("");

        assertThat(DefaultClient.toAcceptHeader(asList(MediaType.parse("text/html"), MediaType.parse("application/xhtml+xml"))))
                .isEqualTo("text/html, application/xhtml+xml");
    }
}
