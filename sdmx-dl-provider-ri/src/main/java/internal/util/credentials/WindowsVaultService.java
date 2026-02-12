package internal.util.credentials;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.sys.OS;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;
import sdmxdl.provider.ri.spi.VaultService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static internal.util.credentials.PowerShellWrapper.exec;
import static java.nio.charset.StandardCharsets.UTF_8;
import static nbbrd.io.sys.ProcessReader.readToString;

@DirectImpl
@ServiceProvider
public final class WindowsVaultService implements VaultService {

    @Override
    public @NonNull String getVaultId() {
        return "WINDOWS_PASSWORD_VAULT";
    }

    @Override
    public boolean isVaultAvailable() {
        return OS.NAME.equals(OS.Name.WINDOWS);
    }

    @Override
    public @Nullable String loadPassword(@NonNull String resource, @NonNull String userName) throws IOException {
        Map<String, String> env = new HashMap<>();
        env.put("RESOURCE", resource);
        env.put("USERNAME", userName);
        String result = readToString(UTF_8, exec(GET, env));
        return !result.isEmpty() ? result : null;
    }

    @Override
    public void storePassword(@NonNull String resource, @NonNull String userName, @Nullable String password) throws IOException {
        if (password != null) {
            if (password.length() > MAX_PASSWORD_SIZE) {
                throw new IOException("Field overflow: password size exceeds " + MAX_PASSWORD_SIZE);
            }
            Map<String, String> env = new HashMap<>();
            env.put("RESOURCE", resource);
            env.put("USERNAME", userName);
            env.put("PASSWORD", password);
            readToString(UTF_8, exec(PUT, env));
        } else {
            Map<String, String> env = new HashMap<>();
            env.put("RESOURCE", resource);
            env.put("USERNAME", userName);
            readToString(UTF_8, exec(REMOVE, env));
        }
    }

    private static final String GET =
            "[void][Windows.Security.Credentials.PasswordVault,Windows.Security.Credentials,ContentType=WindowsRuntime]\n" +
                    "try {\n" +
                    "  echo (New-Object Windows.Security.Credentials.PasswordVault).Retrieve($Env:RESOURCE, $Env:USERNAME).Password\n" +
                    "} catch { exit 0 }\n";

    private static final String PUT =
            "[void][Windows.Security.Credentials.PasswordVault,Windows.Security.Credentials,ContentType=WindowsRuntime]\n" +
                    "$cred = (New-Object Windows.Security.Credentials.PasswordCredential($Env:RESOURCE, $Env:USERNAME, $Env:PASSWORD))\n" +
                    "(New-Object Windows.Security.Credentials.PasswordVault).Add($cred)\n";

    private static final String REMOVE =
            "[void][Windows.Security.Credentials.PasswordVault,Windows.Security.Credentials,ContentType=WindowsRuntime]\n" +
                    "try {\n" +
                    "  $cred = (New-Object Windows.Security.Credentials.PasswordVault).Retrieve($Env:RESOURCE, $Env:USERNAME)\n" +
                    "  (New-Object Windows.Security.Credentials.PasswordVault).Remove($cred)\n" +
                    "} catch { exit 0 }\n";

    // The underlying implementation fails on add if size exceeds some limit (somewhere between 4096*3 and 4096*4)
    // Error: Exception calling "Add" with "1" argument(s): "The file size exceeds the limit allowed and cannot be saved. Cannot add credential to Vault"
    @VisibleForTesting
    static final int MAX_PASSWORD_SIZE = 4096 * 3;
}
