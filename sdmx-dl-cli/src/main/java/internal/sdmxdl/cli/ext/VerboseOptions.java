package internal.sdmxdl.cli.ext;

import picocli.CommandLine;

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

    public void reportToErrorStream(String anchor, String message) {
        if (verbose) {
            CommandLine.Help.ColorScheme colorScheme = spec.commandLine().getColorScheme();
            reportToErrorStream(colorScheme
                    .text("[")
                    .concat(colorScheme.commandText(anchor))
                    .concat(colorScheme.text("] "))
                    .concat(colorScheme.optionText(message))
            );
        }
    }

    public void reportToErrorStream(String anchor, String message, Exception ex) {
        CommandLine.Help.ColorScheme colorScheme = spec.commandLine().getColorScheme();
        reportToErrorStream(colorScheme
                .text("[")
                .concat(colorScheme.commandText(anchor))
                .concat(colorScheme.text("] "))
                .concat(colorScheme.optionText(message))
                .concat(" ")
                .concat(colorScheme.stackTraceText(ex.getMessage()))
        );
    }

    private void reportToErrorStream(CommandLine.Help.Ansi.Text text) {
        spec.commandLine().getErr().println(text);
    }
}
