package internal.util.credentials;

import com.github.tuupertunut.powershelllibjava.PowerShell;
import com.github.tuupertunut.powershelllibjava.PowerShellExecutionException;
import nbbrd.io.Resource;
import nbbrd.picocsv.Csv;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class WinPasswordVault implements Closeable {

    // https://docs.microsoft.com/en-us/uwp/api/windows.security.credentials.passwordcredential
    @lombok.Value
    public static class PasswordCredential {

        @lombok.NonNull
        String resource;

        @lombok.NonNull
        String userName;

        @lombok.NonNull
        char[] password;
    }

    public static WinPasswordVault open() throws IOException {
        WinPasswordVault result = new WinPasswordVault();
        result.exec(loadCode());
        return result;
    }

    @lombok.NonNull
    private final PowerShell psSession;

    private WinPasswordVault() throws IOException {
        psSession = PowerShell.open();
    }

    public PasswordCredential getOrPrompt(String resource, String message, boolean force) throws IOException {
        String resourceParam = PowerShell.escapePowerShellString(resource);
        String messageParam = PowerShell.escapePowerShellString(message);
        String forceParam = force ? "$true" : "$false";
        String result = exec(
                "$cred = GetOrPromptCredential -resource " + resourceParam + " -message " + messageParam + " -force " + forceParam,
                "if ($cred -ne $null) {",
                "  echo ($cred | Select-Object -Property Resource, UserName, Password | ConvertTo-Csv -NoTypeInformation)",
                "}"
        );
        return parse(result);
    }

    public void invalidate(String resource) throws IOException {
        String resourceParam = PowerShell.escapePowerShellString(resource);
        exec("InvalidateCredential -resource " + resourceParam);
    }

    @Override
    public void close() {
        psSession.close();
    }

    private String exec(String... commands) throws IOException {
        try {
            return psSession.executeCommands(commands);
        } catch (PowerShellExecutionException ex) {
            throw new IOException(ex);
        }
    }

    private static PasswordCredential parse(String csv) throws IOException {
        try (Csv.Reader reader = Csv.Reader.of(Csv.Format.DEFAULT, Csv.ReaderOptions.DEFAULT, new StringReader(csv), Csv.DEFAULT_CHAR_BUFFER_SIZE)) {
            if (reader.readLine() && reader.readLine()) {
                if (reader.readField()) {
                    String resource = reader.toString();
                    if (reader.readField()) {
                        String userName = reader.toString();
                        if (reader.readField()) {
                            String password = reader.toString();
                            return new PasswordCredential(resource, userName, password.toCharArray());
                        }
                    }
                }
            }
            return null;
        }
    }

    private static String[] loadCode() throws IOException {
        Optional<InputStream> script = Resource.getResourceAsStream(WinPasswordVault.class, "WinPasswordVault.ps1");
        try (InputStream stream = script.orElseThrow(() -> new IOException("Cannot find WinPasswordVault script"))) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return reader.lines().toArray(String[]::new);
            }
        }
    }
}
