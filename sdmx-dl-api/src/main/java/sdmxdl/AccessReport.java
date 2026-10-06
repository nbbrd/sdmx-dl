package sdmxdl;

import java.net.URI;
import java.time.Duration;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Result of a live access check against a source, as performed by {@link Provider#checkAccess()}.
 *
 * <p>A source is <em>reachable</em> when it answered the check, and <em>accessible</em> when it
 * answered successfully. A source that answers with an error status (e.g. HTTP 5xx) is therefore
 * reachable but not accessible.
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class AccessReport {

    /**
     * Whether the source answered the check, successfully or not.
     */
    boolean reachable;

    /**
     * Whether the source answered the check successfully.
     */
    boolean accessible;

    /**
     * Endpoint that was tested, if known.
     */
    @Nullable URI uri;

    /**
     * Time taken by the check.
     */
    @lombok.Builder.Default
    @NonNull Duration duration = Duration.ZERO;

    /**
     * Status code returned by the source when it answered with an error, if any.
     */
    @Nullable Integer statusCode;

    /**
     * Reason why the source is not accessible, if any.
     */
    @Nullable String errorMessage;
}
