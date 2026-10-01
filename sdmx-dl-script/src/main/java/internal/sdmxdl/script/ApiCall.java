package internal.sdmxdl.script;

import java.util.List;
import java.util.Map;
import lombok.NonNull;

/**
 * Description of a Java library call and of the way its result is reshaped.
 */
@lombok.Value
@lombok.Builder
public class ApiCall {

    public enum Shape {
        /**
         * Data set whose series are flattened to one row per observation.
         */
        DATA_SET,
        /**
         * List of objects mapped to one row per object.
         */
        LIST
    }

    @lombok.Value(staticConstructor = "of")
    public static class Argument {

        /**
         * Name of the request builder method.
         */
        @NonNull String method;

        /**
         * Value passed to the method: a string, an integer or a boolean.
         */
        @NonNull Object value;
    }

    /**
     * Simple name of the request class in the {@code sdmxdl} package.
     */
    @NonNull String requestType;

    /**
     * Name of the provider method that takes the request.
     */
    @NonNull String providerMethod;

    @lombok.Singular
    @NonNull List<Argument> arguments;

    @NonNull Shape shape;

    /**
     * Output columns mapped to getters of the result items; only used with {@link Shape#LIST}.
     */
    @lombok.Singular
    @NonNull Map<String, String> getters;

    @lombok.Singular
    @NonNull List<String> warnings;
}
