package sdmxdl;

import lombok.NonNull;

/**
 * Common supertype of the requests that can be sent to a source.
 * <p>
 * The set of implementations is closed; use {@link #accept(RequestVisitor)} to dispatch on the
 * concrete type.
 */
public interface Request {

    <T> T accept(@NonNull RequestVisitor<T> visitor);
}
