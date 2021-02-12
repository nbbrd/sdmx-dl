package internal.sdmxdl.cli;

import sdmxdl.web.SdmxWebAuthenticator;
import sdmxdl.web.SdmxWebSource;

import java.io.Console;
import java.net.PasswordAuthentication;

@lombok.AllArgsConstructor
final class ConsoleAuthenticator implements SdmxWebAuthenticator {

    @lombok.NonNull
    private final PasswordAuthentication user;

    @Override
    public PasswordAuthentication getPasswordAuthentication(SdmxWebSource source) {
        Console console = System.console();
        if (console == null) return null;
        String username = hasUsername() ? user.getUserName() : console.readLine("Enter username: ");
        char[] password = hasPassword() ? user.getPassword() : console.readPassword("Enter password: ");
        return new PasswordAuthentication(username, password);
    }

    @Override
    public void invalidate(SdmxWebSource source) {
    }

    private boolean hasUsername() {
        return user.getUserName() != null && !user.getUserName().isEmpty();
    }

    private boolean hasPassword() {
        return user.getPassword().length > 0;
    }
}
