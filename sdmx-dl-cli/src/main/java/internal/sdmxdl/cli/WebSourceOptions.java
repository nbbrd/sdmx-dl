/*
 * Copyright 2018 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package internal.sdmxdl.cli;

import picocli.CommandLine;
import sdmxdl.Dataflow;
import sdmxdl.sys.SdmxSystemUtil;
import sdmxdl.web.SdmxWebAuthenticator;
import sdmxdl.web.SdmxWebConnection;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;

import java.io.Console;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
public class WebSourceOptions extends WebOptions {

    @CommandLine.Parameters(
            index = "0",
            paramLabel = "<source>",
            descriptionKey = "sdmxdl.cli.source"
    )
    private String source;

    @CommandLine.Option(
            names = {"--no-sys-auth"},
            defaultValue = "false",
            descriptionKey = "sdmxdl.cli.noSysAuth"
    )
    private boolean noSysAuth;

    @CommandLine.Option(
            names = {"--user"},
            paramLabel = "<user:password>",
            defaultValue = "",
            descriptionKey = "sdmxdl.cli.user",
            converter = UserConverter.class
    )
    private User user;

    public SortedSet<Feature> getSortedFeatures() throws IOException {
        try (SdmxWebConnection conn = getManager().getConnection(getSource())) {
            return conn.isSeriesKeysOnlySupported()
                    ? new TreeSet<>(Collections.singleton(Feature.SERIES_KEYS_ONLY))
                    : Collections.emptySortedSet();
        }
    }

    public List<Dataflow> getSortedFlows() throws IOException {
        try (SdmxWebConnection conn = getManager().getConnection(getSource())) {
            return conn.getFlows()
                    .stream()
                    .sorted(Comparator.comparing(dataflow -> dataflow.getRef().toString()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public SdmxWebManager getManager() throws IOException {
        SdmxWebManager.Builder result = super.getManager()
                .toBuilder()
                .authenticator(getConsoleAuthenticator());

        if (!user.hasUsername() && !isNoSysAuth()) {
            SdmxSystemUtil.configureAuth(result, this::reportIOException);
        }

        return result.build();
    }

    private SdmxWebAuthenticator getConsoleAuthenticator() {
        return new CachedAuthenticator(new ConsoleAuthenticator(user), new ConcurrentHashMap<>());
    }

    @lombok.Value
    private static class User {

        String username;

        char[] password;

        public boolean hasUsername() {
            return username != null && !username.isEmpty();
        }

        public boolean hasPassword() {
            return password != null && password.length != 0;
        }
    }

    private static class UserConverter implements CommandLine.ITypeConverter<User> {

        @Override
        public User convert(String user) {
            if (user == null) {
                return new User(null, null);
            }
            int idx = user.indexOf(':');
            if (idx == -1) {
                return new User(user, null);
            }
            return new User(user.substring(0, idx), user.substring(idx + 1).toCharArray());
        }
    }

    @lombok.AllArgsConstructor
    private static final class ConsoleAuthenticator implements SdmxWebAuthenticator {

        @lombok.NonNull
        private final User user;

        @Override
        public PasswordAuthentication getPasswordAuthentication(SdmxWebSource source) {
            Console console = System.console();
            String username = user.hasUsername() ? user.getUsername() : console.readLine("Enter username: ");
            char[] password = user.hasPassword() ? user.getPassword() : console.readPassword("Enter password: ");
            return new PasswordAuthentication(username, password);
        }

        @Override
        public void invalidate(SdmxWebSource source) {
        }
    }

    @lombok.AllArgsConstructor
    private static final class CachedAuthenticator implements SdmxWebAuthenticator {

        @lombok.NonNull
        private final SdmxWebAuthenticator delegate;

        @lombok.NonNull
        private final ConcurrentHashMap<SdmxWebSource, PasswordAuthentication> cache;

        @Override
        public PasswordAuthentication getPasswordAuthentication(SdmxWebSource source) {
            return cache.computeIfAbsent(source, delegate::getPasswordAuthentication);
        }

        @Override
        public void invalidate(SdmxWebSource source) {
            cache.remove(source);
            delegate.invalidate(source);
        }
    }
}
