package internal.util.credentials;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static nbbrd.io.sys.ProcessReader.readToString;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.NonNull;

final class PowerShellWrapper {

    private PowerShellWrapper() {
        // static class
    }

    private static final String UTF_8_OUTPUT =
            "[Console]::OutputEncoding = New-Object System.Text.UTF8Encoding $false\n";

    /**
     * Runs a script and returns its output.
     * The process is killed if it does not end within the timeout or if the current thread is interrupted.
     */
    public static @NonNull String execToString(
            @NonNull String script, @NonNull Map<String, String> env, @NonNull Duration timeout) throws IOException {
        Process process = exec(script, env);
        try {
            // NB: the output is read concurrently so that a full pipe cannot block the process
            FutureTask<String> output = new FutureTask<>(() -> readToString(UTF_8, process));
            Thread reader = new Thread(output, "powershell-reader");
            reader.setDaemon(true);
            reader.start();
            return output.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            InterruptedIOException result = new InterruptedIOException("Interrupted while waiting for PowerShell");
            result.initCause(ex);
            throw result;
        } catch (TimeoutException ex) {
            throw new IOException("No answer from PowerShell within " + timeout, ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            throw cause instanceof IOException ? (IOException) cause : new IOException(cause);
        } finally {
            process.destroyForcibly();
        }
    }

    // NB: PowerShell is started directly (not through cmd) so that killing the process also closes its dialogs
    private static Process exec(String script, Map<String, String> env) throws IOException {
        List<String> command = new ArrayList<>();
        command.add("powershell");
        command.add("-NoProfile");
        command.add("-ExecutionPolicy");
        command.add("Bypass");
        command.add("-NoLogo");
        command.add("-EncodedCommand");
        command.add(Base64.getEncoder().encodeToString((UTF_8_OUTPUT + script).getBytes(UTF_16LE)));
        ProcessBuilder result = new ProcessBuilder(command);
        result.environment().putAll(env);
        return result.start();
    }
}
