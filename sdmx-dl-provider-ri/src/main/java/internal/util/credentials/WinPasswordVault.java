package internal.util.credentials;

import com.github.tuupertunut.powershelllibjava.PowerShell;
import com.github.tuupertunut.powershelllibjava.PowerShellExecutionException;
import lombok.NonNull;
import nbbrd.io.picocsv.Picocsv;
import nbbrd.io.text.TextParser;
import nbbrd.io.text.TextResource;
import nbbrd.picocsv.Csv;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class WinPasswordVault implements Closeable {

    // https://docs.microsoft.com/en-us/uwp/api/windows.security.credentials.passwordcredential
    @lombok.Value
    public static class PasswordCredential {

        @NonNull String resource;

        @NonNull String userName;

        @NonNull char[] password;
    }

    public static @NonNull WinPasswordVault open() throws IOException {
        WinPasswordVault result = new WinPasswordVault();
        result.exec(loadCode());
        return result;
    }

    private final @NonNull PowerShell psSession;

    private WinPasswordVault() throws IOException {
        psSession = PowerShell.open();
    }

    public @NonNull PasswordCredential getOrPrompt(@NonNull String resource, @NonNull String message, boolean force) throws IOException {
        String resourceParam = PowerShell.escapePowerShellString(resource);
        String messageParam = PowerShell.escapePowerShellString(message);
        String forceParam = force ? "$true" : "$false";
        String result = exec(
                "$cred = GetOrPromptCredential -resource " + resourceParam + " -message " + messageParam + " -force " + forceParam,
                "if ($cred -ne $null) {",
                "  echo ($cred | Select-Object -Property Resource, UserName, Password | ConvertTo-Csv -NoTypeInformation)",
                "}"
        );
        return CREDENTIAL_PARSER.parseChars(result);
    }

    public void invalidate(@NonNull String resource) throws IOException {
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

    private static final TextParser<PasswordCredential> CREDENTIAL_PARSER = Picocsv.Parser.builder(WinPasswordVault::parseCsv).build();

    private static PasswordCredential parseCsv(Csv.Reader reader) throws IOException {
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
        throw new IOException("Invalid content");
    }

    private static String[] loadCode() throws IOException {
        try (BufferedReader reader = TextResource.newBufferedReader(WinPasswordVault.class, "WinPasswordVault.ps1", UTF_8.newDecoder())) {
            return reader.lines().toArray(String[]::new);
        }
    }
}
