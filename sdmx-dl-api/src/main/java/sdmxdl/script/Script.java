package sdmxdl.script;

import java.util.List;
import lombok.NonNull;

/**
 * Script generated for a request.
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class Script {

    /**
     * Target used to generate this script.
     */
    @NonNull ScriptTarget target;

    /**
     * File extension of the script, without leading dot (e.g. {@code py}).
     */
    @NonNull String fileExtension;

    /**
     * Media type of the script, without parameters (e.g. {@code text/x-python}).
     */
    @NonNull String mediaType;

    /**
     * Content of the script.
     */
    @NonNull String content;

    /**
     * Request parts that could not be expressed with the target and were therefore ignored.
     */
    @lombok.Singular
    @NonNull List<String> warnings;
}
