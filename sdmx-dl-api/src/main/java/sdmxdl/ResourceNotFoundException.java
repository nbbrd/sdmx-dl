package sdmxdl;

import java.io.IOException;

/**
 * Signals that a requested resource (source, database, flow, structure, codelist, dimension,
 * concept, ...) does not exist.
 *
 * <p>It extends {@link IOException} so that callers that only expect I/O failures keep working,
 * while callers that need to tell "not found" apart from other failures can catch it.
 */
public final class ResourceNotFoundException extends IOException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
