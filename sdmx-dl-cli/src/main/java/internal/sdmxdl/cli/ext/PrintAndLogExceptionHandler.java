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
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName();
        cmd.getErr().println(cmd.getColorScheme().errorText(errorMessage));
//        cmd.getErr().println(cmd.getColorScheme().stackTraceText(ex));
        return cmd.getExitCodeExceptionMapper() != null
                ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
                : cmd.getCommandSpec().exitCodeOnExecutionException();
    }
}
