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

    public void reportToErrorStream(String anchor, String message) {
        System.err.println(anchor + ": " + message);
    }

    public void reportToErrorStream(String anchor, String message, Exception ex) {
        System.err.println(anchor + ": " + message + " - " + ex.getMessage());
    }
}
