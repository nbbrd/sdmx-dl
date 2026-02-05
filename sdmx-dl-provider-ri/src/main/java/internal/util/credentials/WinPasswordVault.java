package internal.util.credentials;

import com.github.tuupertunut.powershelllibjava.PowerShell;
import com.github.tuupertunut.powershelllibjava.PowerShellExecutionException;
import lombok.NonNull;
import nbbrd.design.NotThreadSafe;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.io.picocsv.Picocsv;
import nbbrd.io.text.TextParser;
import nbbrd.io.text.TextResource;
import nbbrd.picocsv.Csv;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@NotThreadSafe
public final class WinPasswordVault implements Closeable {

    // https://docs.microsoft.com/en-us/uwp/api/windows.security.credentials.passwordcredential
    @lombok.Value
    public static class PasswordCredential {

        @NonNull
        String resource;

        @NonNull
        String userName;

        @NonNull
        char[] password;
    }

    @StaticFactoryMethod
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

    public @Nullable PasswordCredential get(@NonNull String resource) throws IOException {
        String resourceParam = PowerShell.escapePowerShellString(resource);
        String result = exec(
                "$cred = GetCredential -resource " + resourceParam,
                "if ($cred -ne $null) {",
                "  echo ($cred | Select-Object -Property Resource, UserName, Password | ConvertTo-Csv -NoTypeInformation)",
                "}"
        );
        return !result.isEmpty() ? CREDENTIAL_PARSER.parseChars(result) : null;
    }

    public void add(@NonNull PasswordCredential credential) throws IOException {
        String resourceParam = PowerShell.escapePowerShellString(credential.getResource());
        String userNameParam = PowerShell.escapePowerShellString(credential.getUserName());
        String passwordParam = PowerShell.escapePowerShellString(String.valueOf(credential.getPassword()));
        exec(
                "$cred = (New-Object Windows.Security.Credentials.PasswordCredential(" + resourceParam + ", " + userNameParam + ", " + passwordParam + "))",
                "AddCredential -cred $cred"
        );
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

    // The underlying implementation fails on add if size exceeds some limit (somewhere between 4096*3 and 4096*4)
    // Error: Exception calling "Add" with "1" argument(s): "The file size exceeds the limit allowed and cannot be saved. Cannot add credential to Vault"
    public static final int MAX_PASSWORD_SIZE = 4096 * 3;

    private static final TextParser<PasswordCredential> CREDENTIAL_PARSER = Picocsv.Parser
            .builder(WinPasswordVault::parseCredential)
            .options(Csv.ReaderOptions.DEFAULT.toBuilder().maxCharsPerField(MAX_PASSWORD_SIZE).build())
            .build();

    private static PasswordCredential parseCredential(Csv.Reader csv) throws IOException {
        if (!csv.readLine()) throw new IOException("Missing CSV header line");
        if (!csv.readLine()) throw new IOException("Missing CSV data line");

        if (!csv.readField()) throw new IOException("Missing 'Resource' field in credential data");
        String resource = csv.toString();

        if (!csv.readField()) throw new IOException("Missing 'UserName' field in credential data");
        String userName = csv.toString();

        if (!csv.readField()) throw new IOException("Missing 'Password' field in credential data");
        String password = csv.toString();

        return new PasswordCredential(resource, userName, password.toCharArray());
    }

    private static String[] loadCode() throws IOException {
        try (BufferedReader reader = TextResource.newBufferedReader(WinPasswordVault.class, "WinPasswordVault.ps1", UTF_8)) {
            return reader.lines().toArray(String[]::new);
        }
    }
}
