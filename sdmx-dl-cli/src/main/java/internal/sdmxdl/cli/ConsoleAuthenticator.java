package internal.sdmxdl.cli;

import lombok.NonNull;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebAuthenticator;

import java.io.Console;
import java.io.IOError;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.concurrent.ConcurrentHashMap;

final class ConsoleAuthenticator implements WebAuthenticator {

    private final Console console = System.console();

    private final ConcurrentHashMap<SdmxWebSource, PasswordAuthentication> cache = new ConcurrentHashMap<>();

    @Override
    public @NonNull String getId() {
        return "CONSOLE";
    }

    @Override
    public boolean isAvailable() {
        return isConsoleAvailable();
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication(SdmxWebSource source) throws IOException {
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
    public void invalidate(SdmxWebSource source) {
        cache.remove(source);
    }

    private PasswordAuthentication readPasswordAuthentication(SdmxWebSource source) throws IOError {
        console.format("Enter your credentials for %s\n", source.getId());
        String username = console.readLine("Enter username: ");
        char[] password = console.readPassword("Enter password: ");
        return new PasswordAuthentication(username, password);
    }
}
