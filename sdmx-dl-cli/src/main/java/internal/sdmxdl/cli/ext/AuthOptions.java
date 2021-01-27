package internal.sdmxdl.cli.ext;

import picocli.CommandLine;

import java.net.PasswordAuthentication;

@lombok.Getter
@lombok.Setter
public class AuthOptions {

    @CommandLine.Option(
            names = {"--no-system-auth"},
            defaultValue = "false",
            descriptionKey = "cli.noSystemAuth"
    )
    private boolean noSysAuth;

    @CommandLine.Option(
            names = {"--user"},
            paramLabel = "<user:password>",
            defaultValue = "",
            descriptionKey = "cli.user",
            converter = UserConverter.class
    )
    private PasswordAuthentication user;

    private static final class UserConverter implements CommandLine.ITypeConverter<PasswordAuthentication> {

        @Override
        public PasswordAuthentication convert(String user) {
            if (user == null) {
                return new PasswordAuthentication(null, new char[0]);
            }
            int idx = user.indexOf(':');
            if (idx == -1) {
                return new PasswordAuthentication(user, new char[0]);
            }
            return new PasswordAuthentication(user.substring(0, idx), user.substring(idx + 1).toCharArray());
        }
    }
}
