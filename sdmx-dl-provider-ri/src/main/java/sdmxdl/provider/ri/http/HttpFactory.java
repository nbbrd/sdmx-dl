package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.design.ThreadSafe;
import nbbrd.io.http.HttpClient;
import nbbrd.io.text.BaseProperty;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.util.List;

/**
 * Factory for creating and configuring {@link HttpClient} instances.
 * <p>
 * Implementations define the HTTP client creation strategy and expose the properties
 * that can be configured for a particular factory type.
 * </p>
 */
@ThreadSafe
public interface HttpFactory {

    /**
     * Gets the name of this factory.
     *
     * @return the factory name identifier, never null
     */
    @NonNull
    String getFactoryName();

    /**
     * Gets the list of configurable properties for this factory.
     *
     * @return list of properties supported by this factory, never null
     */
    @NonNull
    List<BaseProperty> getHttpClientProperties();

    /**
     * Creates a new HTTP client configured for the given web source and context.
     *
     * @param source the web source providing configuration and properties
     * @param context the web context containing network, authentication, and event listeners
     * @return a configured HTTP client, never null
     */
    @NonNull
    HttpClient createHttpClient(@NonNull WebSource source, @NonNull WebContext context);
}
