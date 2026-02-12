package internal.util.credentials;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.io.sys.OS;
import nbbrd.io.text.TextResource;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;
import sdmxdl.provider.ri.spi.PasswordPrompt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static internal.util.credentials.PowerShellWrapper.exec;
import static java.nio.charset.StandardCharsets.UTF_8;
import static nbbrd.io.sys.ProcessReader.readToString;

@DirectImpl
@ServiceProvider
public final class WindowsPasswordPrompt implements PasswordPrompt {

    @Override
    public @NonNull String getPromptId() {
        return "WINDOWS_PROMPT";
    }

    @Override
    public boolean isPromptAvailable() {
        return OS.NAME.equals(OS.Name.WINDOWS);
    }

    @Override
    public int getPromptRank() {
        return 200;
    }

    @Override
    public @Nullable PasswordAuthentication promptCredentials(@NonNull String caption, @NonNull String message) throws IOException {
        Map<String, String> env = new HashMap<>();
        env.put("CAPTION", caption);
        env.put("MESSAGE", message);
        String secret = readToString(UTF_8, exec(PROMPT, env));
        int separatorIndex = secret.indexOf(':');
        if (separatorIndex > 0) {
            String username = secret.substring(0, separatorIndex);
            String password = secret.substring(separatorIndex + 1);
            return new PasswordAuthentication(username, password.toCharArray());
        }
        return null;
    }

    private static final String PROMPT = loadCode();

    // Cannot use $host.ui.PromptForCredential since a mysterious bug on Windows 11 prevents the dialog to popup in some cases
    private static String loadCode() {
        try (BufferedReader reader = TextResource.newBufferedReader(WindowsPasswordPrompt.class, "CredUIPromptForCredentials.ps1", UTF_8)) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
