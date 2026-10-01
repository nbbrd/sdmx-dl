package internal.sdmxdl.script;

import java.util.List;
import java.util.Map;
import lombok.NonNull;

/**
 * Language-agnostic description of a REST call and of the way its JSON response is reshaped.
 */
@lombok.Value
@lombok.Builder
public class RestCall {

    public enum Shape {
        /**
         * Data set object whose series are flattened to one row per observation.
         */
        DATA_SET,
        /**
         * Array of objects mapped to one row per object.
         */
        LIST
    }

    /**
     * Path relative to the endpoint, already percent-encoded and starting with a slash.
     */
    @NonNull String path;

    /**
     * Query parameters whose values are either strings or integers.
     */
    @lombok.Singular
    @NonNull Map<String, Object> parameters;

    @NonNull Shape shape;

    /**
     * Output columns mapped to JSON fields; only used with {@link Shape#LIST}.
     */
    @lombok.Singular
    @NonNull Map<String, String> fields;

    @lombok.Singular
    @NonNull List<String> warnings;
}
