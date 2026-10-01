package internal.sdmxdl.script;

import java.util.List;
import lombok.NonNull;

/**
 * Language-agnostic description of a CLI call and of the CSV columns to keep from its output.
 */
@lombok.Value
@lombok.Builder
public class CliCall {

    @lombok.Singular
    @NonNull List<String> arguments;

    @lombok.Singular
    @NonNull List<String> columns;

    @lombok.Singular
    @NonNull List<String> warnings;
}
