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
        spec.commandLine().getErr().println(anchor + ": " + message);
    }

    public void reportToErrorStream(String anchor, String message, Exception ex) {
        spec.commandLine().getErr().println(anchor + ": " + message + " - " + ex.getMessage());
    }
}
