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
 *
 * @author Philippe Charles
 */
@lombok.Data
public class WebSourceOptions extends WebOptions {

    @CommandLine.Parameters(
            index = "0",
            paramLabel = "<source>",
            descriptionKey = "sdmxdl.cli.source"
    )
    private String source;

    @CommandLine.Option(
            names = {"--user"},
            paramLabel = "<user:password>",
            descriptionKey = "sdmxdl.cli.user"
    )
    private String user;

    public SortedSet<Feature> getFeatures() throws IOException {
        try (SdmxWebConnection conn = getManager().getConnection(getSource())) {

            return conn.isSeriesKeysOnlySupported()
                    ? new TreeSet<>(Collections.singleton(Feature.SERIES_KEYS_ONLY))
                    : Collections.emptySortedSet();
        }
    }

    public List<Dataflow> getFlows() throws IOException {
        try (SdmxWebConnection conn = getManager().getConnection(getSource())) {
            return conn.getFlows()
                    .stream()
                    .sorted(Comparator.comparing(dataflow -> dataflow.getRef().toString()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public SdmxWebManager getManager() throws IOException {
        return super.getManager().withAuthenticator(getAuthenticator());
    }

    private SdmxWebAuthenticator getAuthenticator() {
        return new CachedAuthenticator(ConsoleAuthenticator.of(user), new ConcurrentHashMap<>());
    }

    @lombok.AllArgsConstructor
    private static final class ConsoleAuthenticator implements SdmxWebAuthenticator {

        public static ConsoleAuthenticator of(String user) {
            if (user == null) {
                return new ConsoleAuthenticator(null, null);
            }
            int idx = user.indexOf(':');
            if (idx == -1) {
                return new ConsoleAuthenticator(user, null);
            }
            String username = user.substring(0, idx);
            char[] password = user.substring(idx + 1).toCharArray();
            return new ConsoleAuthenticator(username, password);
        }

        private final String optUsername;

        private final char[] optPassword;

        @Override
        public PasswordAuthentication getPasswordAuthentication(SdmxWebSource source) {
            Console console = System.console();
            String username = optUsername != null ? optUsername : console.readLine("Enter username: ");
            char[] password = optPassword != null ? optPassword : console.readPassword("Enter password: ");
            return new PasswordAuthentication(username, password);
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
    }
}
