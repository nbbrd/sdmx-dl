package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.io.http.HttpClient;
import nbbrd.io.text.BaseProperty;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Support implementation for building decorator instances.
 * <p>
 * Provides a builder pattern for creating decorators with optional properties and a factory
 * method for creating and decorating HTTP clients. This class implements both
 * {@link HttpDecoration} and {@link HttpFactory} to simplify decorator implementation.
 * </p>
 */
@lombok.Builder
public final class HttpDecorationSupport implements HttpDecoration {

    /**
     * The name of this decorator.
     */
    private final @NonNull String name;

    /**
     * The list of properties supported by this decorator.
     */
    @lombok.Singular
    private final @NonNull List<BaseProperty> properties;

    /**
     * The factory function that creates and decorates HTTP clients.
     */
    private final @NonNull SuperFactory superFactory;

    /**
     * Gets the name of this decorator.
     *
     * @return the decorator name identifier
     */
    @Override
    public @NonNull String getDecoratorName() {
        return name;
    }

    /**
     * Gets the list of configurable properties for this decorator.
     *
     * @return the list of properties
     */
    @Override
    public @NonNull List<BaseProperty> getDecoratorProperties() {
        return properties;
    }

    /**
     * Decorates the given factory by wrapping it with this decorator's functionality.
     * <p>
     * The returned factory combines the properties of both the original factory and this decorator,
     * and applies the decoration logic when creating clients.
     * </p>
     *
     * @param factory the factory to decorate
     * @return a decorated factory combining both the original and decorator behavior
     */
    @Override
    public @NonNull HttpFactory decorate(@NonNull HttpFactory factory) {
        return new HttpFactory() {

            /**
             * Gets the decorated factory name.
             *
             * @return the original factory name plus decorator info
             */
            @Override
            public @NonNull String getFactoryName() {
                return factory.getFactoryName() + " with " + name;
            }

            /**
             * Gets all properties from both the original factory and this decorator.
             *
             * @return the combined list of properties
             */
            @Override
            public @NonNull List<BaseProperty> getFactoryProperties() {
                return Stream.concat(factory.getFactoryProperties().stream(), getDecoratorProperties().stream()).collect(toList());
            }

            /**
             * Creates an HTTP client by decorating the original factory's client.
             *
             * @param source the web source configuration
             * @param context the web context
             * @return a decorated HTTP client
             */
            @Override
            public @NonNull HttpClient create(@NonNull WebSource source, @NonNull WebContext context) {
                return superFactory.createAndDecorate(factory, source, context);
            }
        };
    }

    /**
     * Functional interface for client creation and decoration.
     * <p>
     * Implementations specify how to create an HTTP client from a factory and apply
     * decorator-specific functionality to it.
     * </p>
     */
    @FunctionalInterface
    public interface SuperFactory {

        /**
         * Creates an HTTP client and applies decorator-specific functionality.
         *
         * @param delegate the underlying HTTP client factory
         * @param source the web source configuration
         * @param context the web context
         * @return a decorated HTTP client
         */
        @NonNull
        HttpClient createAndDecorate(@NonNull HttpFactory delegate, @NonNull WebSource source, @NonNull WebContext context);
    }
}
