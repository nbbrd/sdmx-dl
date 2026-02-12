package internal.util.credentials;

import lombok.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_16LE;

final class PowerShellWrapper {

    private PowerShellWrapper() {
        // static class
    }

    public static @NonNull Process exec(@NonNull String script, @NonNull Map<String, String> env) throws IOException {
        List<String> command = new ArrayList<>();
        command.add("cmd");
        command.add("/c");
        command.add("chcp 65001 > NUL"); // UTF-8
        command.add("&");
        command.add("powershell");
        command.add("-NoProfile");
        command.add("-ExecutionPolicy");
        command.add("Bypass");
        command.add("-NoLogo");
        command.add("-EncodedCommand");
        command.add(Base64.getEncoder().encodeToString(script.getBytes(UTF_16LE)));
        ProcessBuilder result = new ProcessBuilder(command);
        result.environment().putAll(env);
        return result.start();
    }
}
