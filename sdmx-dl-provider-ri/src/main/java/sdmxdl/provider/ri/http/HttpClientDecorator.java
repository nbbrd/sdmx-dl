package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.design.ThreadSafe;
import nbbrd.io.text.BaseProperty;

import java.util.List;

/**
 * Decorator interface for enhancing {@link HttpClientFactory} with additional capabilities.
 * <p>
 * Implementations provide a way to wrap an existing HTTP client factory with additional
 * behavior such as caching, logging, byte counting, or other cross-cutting concerns.
 * Each decorator exposes its own set of configurable properties.
 * </p>
 */
@ThreadSafe
public interface HttpClientDecorator {

    /**
     * Gets the name of this decorator.
     *
     * @return the decorator name identifier, never null
     */
    @NonNull
    String getDecoratorName();

    /**
     * Gets the list of configurable properties for this decorator.
     *
     * @return list of properties supported by this decorator, never null
     */
    @NonNull
    List<BaseProperty> getDecoratorProperties();

    /**
     * Decorates the given HTTP client factory with this decorator's functionality.
     * <p>
     * Returns a new factory that wraps the provided factory, adding additional behavior
     * while preserving the original factory's functionality.
     * </p>
     *
     * @param factory the factory to decorate
     * @return a decorated factory combining both the original and decorator behavior, never null
     */
    @NonNull
    HttpClientFactory decorate(@NonNull HttpClientFactory factory);
}
