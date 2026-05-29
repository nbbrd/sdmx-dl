package internal.sdmxdl.cli.ext;

import org.jspecify.annotations.Nullable;
import picocli.CommandLine;
import sdmxdl.web.WebSource;

@lombok.Getter
@lombok.Setter
public class VerboseOptions {

    @CommandLine.Option(
            names = {"-v", "--verbose"},
            defaultValue = "false",
            descriptionKey = "cli.verbose"
    )
    private boolean verbose;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;


    public void reportToErrorStream(@Nullable WebSource source, @Nullable String marker, CharSequence message) {
        if (verbose) {
            printToErrorStream(source, marker, message, null);
        }
    }

    public void reportToErrorStream(@Nullable WebSource source, @Nullable String marker, CharSequence message, Exception ex) {
        if (verbose) {
            printToErrorStream(source, marker, message, ex);
        }
    }

    private void printToErrorStream(@Nullable WebSource source, @Nullable String marker, CharSequence message, @Nullable Exception ex) {
        CommandLine.Help.ColorScheme colorScheme = spec.commandLine().getColorScheme();

        CommandLine.Help.Ansi.Text result = colorScheme
                .text("[")
                .concat(colorScheme.commandText(source != null ? source.getId() : "-"))
                .concat(colorScheme.text("] ("))
                .concat(colorScheme.optionText(marker))
                .concat(colorScheme.text(") "))
                .concat(colorScheme.text(message.toString()));

        if (ex != null) {
            String details = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName();
            result = result.concat(" ").concat(colorScheme.stackTraceText(details));
        }

        spec.commandLine().getErr().println(result);
    }
}
