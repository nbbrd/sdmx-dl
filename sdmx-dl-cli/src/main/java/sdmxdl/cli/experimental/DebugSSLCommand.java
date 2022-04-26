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
package sdmxdl.cli.experimental;

import internal.sdmxdl.cli.DebugOutputOptions;
import nl.altindag.ssl.util.KeyStoreUtils;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.security.KeyStore;
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "ssl", description = "Debug SSL")
@SuppressWarnings("FieldMayBeFinal")
public final class DebugSSLCommand implements Callable<Void> {

    @CommandLine.ArgGroup(validate = false, headingKey = "debug")
    private DebugOutputOptions output = new DebugOutputOptions();

    @Override
    public Void call() throws Exception {
        try (BufferedWriter writer = new BufferedWriter(output.newCharWriter())) {
            writer.write("Providers: ");
            writer.write(Arrays.toString(Security.getProviders()));
            writer.newLine();
            for (KeyStore keyStore : KeyStoreUtils.loadSystemKeyStores()) {
                writer.write("KeyStore aliases: ");
                writer.write(Collections.list(keyStore.aliases()).toString());
                writer.newLine();
            }
        }
        return null;
    }
}
