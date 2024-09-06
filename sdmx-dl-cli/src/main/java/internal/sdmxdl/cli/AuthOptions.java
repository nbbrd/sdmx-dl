package internal.sdmxdl.cli;

import internal.sdmxdl.web.spi.AuthenticatorLoader;
import picocli.CommandLine;
import sdmxdl.web.spi.Authenticator;

import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@lombok.Getter
@lombok.Setter
public class AuthOptions {

    @CommandLine.Option(
            names = {"--no-system-auth"},
            defaultValue = "false",
            descriptionKey = "cli.noSystemAuth"
    )
    private boolean noSystemAuth;

    @CommandLine.Option(
            names = {"--user"},
            paramLabel = "<user:password>",
            defaultValue = "",
            descriptionKey = "cli.user",
            converter = UserConverter.class
    )
    private PasswordAuthentication user = UserConverter.getNoUser();

    public boolean hasUsername() {
        return user.getUserName() != null && !user.getUserName().isEmpty();
    }

    public boolean hasPassword() {
        return user.getPassword().length > 0;
    }

    private static final class UserConverter implements CommandLine.ITypeConverter<PasswordAuthentication> {

        @Override
        public PasswordAuthentication convert(String user) {
            if (user == null || user.equals("")) {
                return getNoUser();
            }
            int idx = user.indexOf(':');
            if (idx == -1) {
                return new PasswordAuthentication(user, new char[0]);
            }
            return new PasswordAuthentication(user.substring(0, idx), user.substring(idx + 1).toCharArray());
        }

        static PasswordAuthentication getNoUser() {
            return new PasswordAuthentication(null, new char[0]);
        }
    }

    public List<Authenticator> getAuthenticators() {
        if (hasUsername() && hasPassword()) {
            return Collections.singletonList(new ConstantAuthenticator(getUser()));
        }
        List<Authenticator> result = new ArrayList<>();
        if (!isNoSystemAuth()) {
            result.addAll(AuthenticatorLoader.load());
        }
        if (result.isEmpty()) {
            ConsoleAuthenticator fallback = new ConsoleAuthenticator();
            if (fallback.isAuthenticatorAvailable()) {
                result.add(fallback);
            }
        }
        return result;
    }
}
