package internal.sdmxdl.cli.ext;

import picocli.CommandLine;

import java.util.logging.Level;
import java.util.logging.Logger;

@lombok.AllArgsConstructor
public final class PrintAndLogExceptionHandler implements CommandLine.IExecutionExceptionHandler {

    @lombok.NonNull
    private final Class<?> logAnchor;

    @Override
    public int handleExecutionException(Exception ex, CommandLine cmd, CommandLine.ParseResult parseResult) throws Exception {
        Logger.getLogger(logAnchor.getName()).log(Level.SEVERE, "While executing command", ex);
        cmd.getErr().println(cmd.getColorScheme().errorText(ex.getMessage()));
        return cmd.getExitCodeExceptionMapper() != null
                ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
                : cmd.getCommandSpec().exitCodeOnExecutionException();
    }
}
