package internal.sdmxdl.cli;

import lombok.NonNull;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Authenticator;

import java.io.Console;
import java.io.IOError;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.concurrent.ConcurrentHashMap;

final class ConsoleAuthenticator implements Authenticator {

    private final Console console = System.console();

    private final ConcurrentHashMap<WebSource, PasswordAuthentication> cache = new ConcurrentHashMap<>();

    @Override
    public @NonNull String getAuthenticatorId() {
        return "CONSOLE";
    }

    @Override
    public boolean isAuthenticatorAvailable() {
        return isConsoleAvailable();
    }

    @Override
    public PasswordAuthentication getPasswordAuthenticationOrNull(@NonNull WebSource source) throws IOException {
        if (!isConsoleAvailable()) {
            throw new IOException("Console is not available");
        }
        try {
            return cache.computeIfAbsent(source, this::readPasswordAuthentication);
        } catch (IOError ex) {
            if (ex.getCause() instanceof IOException) {
                throw (IOException) ex.getCause();
            }
            throw new IOException(ex);
        }
    }

    private boolean isConsoleAvailable() {
        return console != null;
    }

    @Override
    public void invalidateAuthentication(@NonNull WebSource source) {
        cache.remove(source);
    }

    private PasswordAuthentication readPasswordAuthentication(WebSource source) throws IOError {
        console.format("Enter your credentials for %s\n", source.getId());
        String username = console.readLine("Enter username: ");
        char[] password = console.readPassword("Enter password: ");
        return new PasswordAuthentication(username, password);
    }
}
