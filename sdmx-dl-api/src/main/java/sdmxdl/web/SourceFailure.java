package sdmxdl.web;

import lombok.NonNull;
import sdmxdl.DatabaseRef;

/**
 * Failure of a source, or of one of its databases, during a request that spans several sources.
 */
@lombok.Value
public class SourceFailure {

    /**
     * Id of the failing source.
     */
    @NonNull String source;

    /**
     * Failing database, or {@link DatabaseRef#NO_DATABASE} if the whole source failed.
     */
    @NonNull DatabaseRef database;

    /**
     * Cause of the failure; a {@link java.util.concurrent.TimeoutException} if the source did
     * not answer in time.
     */
    @NonNull Exception cause;
}
