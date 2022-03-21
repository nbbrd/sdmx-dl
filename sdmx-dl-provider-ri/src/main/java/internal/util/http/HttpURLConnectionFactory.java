package internal.util.http;

import lombok.NonNull;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceFilter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

@ServiceDefinition(
        quantifier = Quantifier.SINGLE,
        fallback = DefaultHttpURLConnectionFactory.class,
        singleton = true
)
public interface HttpURLConnectionFactory {

    default @NonNull String getName() {
        return getClass().getSimpleName();
    }

    @ServiceFilter
    default boolean isRequired() {
        String backend = System.getProperty(SDMXDL_RI_WEB_BACKEND);
        return backend != null && backend.equalsIgnoreCase(getName());
    }

    @NonNull HttpURLConnection openConnection(@NonNull URL url, @NonNull Proxy proxy) throws IOException;

    String SDMXDL_RI_WEB_BACKEND = "sdmxdl.ri.web.backend";
}
