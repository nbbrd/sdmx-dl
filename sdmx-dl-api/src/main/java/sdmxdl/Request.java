package sdmxdl;

import lombok.NonNull;

/**
 * Common supertype of the requests that can be sent to a source.
 * <p>
 * The set of implementations is closed: this interface is not intended to be implemented outside
 * sdmx-dl. Use {@link #accept(RequestVisitor)} to dispatch on the concrete type.
 */
public interface Request {

    /**
     * Dispatches this request to the method of the visitor that matches its concrete type.
     *
     * @param visitor the visitor
     * @param <T>     the type of the result
     * @return the result of the visitor, which may be null if the visitor allows it
     */
    <T> T accept(@NonNull RequestVisitor<T> visitor);
}
