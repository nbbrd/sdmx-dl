package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.io.http.HttpClient;
import nbbrd.io.text.BaseProperty;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.util.List;

/**
 * Support implementation for building {@link HttpFactory} instances.
 * <p>
 * Provides a builder pattern for creating factory instances with a name, optional properties,
 * and a supplier function for creating HTTP clients. This simplifies the implementation
 * of custom factories by delegating common boilerplate via {@code @lombok.experimental.Delegate}.
 * </p>
 *
 * @see HttpFactory
 * @see DefaultHttpFactory
 */
@lombok.Builder
public final class HttpFactorySupport implements HttpFactory {

    /**
     * The name of this factory.
     */
    private final @NonNull String name;

    /**
     * The list of properties supported by this factory.
     */
    @lombok.Singular
    private final List<BaseProperty> properties;

    /**
     * The supplier function that creates HTTP clients.
     */
    private final @NonNull Supplier supplier;

    /**
     * Gets the name of this factory.
     *
     * @return the factory name identifier
     */
    @Override
    public @NonNull String getFactoryName() {
        return name;
    }

    /**
     * Gets the list of configurable properties for this factory.
     *
     * @return the list of properties
     */
    @Override
    public @NonNull List<BaseProperty> getHttpClientProperties() {
        return properties;
    }

    /**
     * Creates a new HTTP client configured for the given web source and context.
     * <p>
     * Delegates to the configured {@link Supplier} to perform the actual client creation.
     * </p>
     *
     * @param source the web source providing configuration and properties
     * @param context the web context containing network, authentication, and event listeners
     * @return a configured HTTP client
     */
    @Override
    public @NonNull HttpClient createHttpClient(@NonNull WebSource source, @NonNull WebContext context) {
        return supplier.create(source, context);
    }

    /**
     * Functional interface for HTTP client creation.
     * <p>
     * Implementations specify how to create an HTTP client from a web source and context.
     * </p>
     */
    @FunctionalInterface
    public interface Supplier {

        /**
         * Creates an HTTP client for the given web source and context.
         *
         * @param source the web source providing configuration and properties
         * @param context the web context containing runtime configuration
         * @return a configured HTTP client
         */
        @NonNull
        HttpClient create(@NonNull WebSource source, @NonNull WebContext context);
    }
}
