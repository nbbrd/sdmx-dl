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

import internal.sdmxdl.cli.SortOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import nbbrd.io.text.Formatter;
import nl.altindag.ssl.util.KeyStoreUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Security;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "ssl", description = "Debug SSL")
@SuppressWarnings("FieldMayBeFinal")
public final class DebugSSLCommand implements Callable<Void> {

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Mixin
    private SortOptions sort;

    @Override
    public Void call() throws Exception {
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<Map.Entry<String, String>> getTable() {
        CsvTable.Builder<Map.Entry<String, String>> result = CsvTable.builder();
        result.columnOf("Type", Map.Entry::getKey);
        result.columnOf("Value", Map.Entry::getValue);
        return result.build();
    }

    private Stream<Map.Entry<String, String>> getRows() {
        Comparator<Map.Entry<String, String>> first = Map.Entry.comparingByKey();
        Comparator<Map.Entry<String, String>> second = Map.Entry.comparingByValue();
        return sort.applySort(Stream.concat(getSecurityProvider(), getKeyStoreAlias()), first.thenComparing(second));
    }

    private Stream<Map.Entry<String, String>> getSecurityProvider() {
        return Stream.of(Security.getProviders())
                .map(provider -> entry("Security provider", provider.toString()));
    }

    private Stream<Map.Entry<String, String>> getKeyStoreAlias() {
        return KeyStoreUtils.loadSystemKeyStores().stream()
                .flatMap(keyStore -> aliases(keyStore).map(alias -> entry("KeyStore alias", alias)));
    }

    private static Map.Entry<String, String> entry(String key, String value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    private static Stream<String> aliases(KeyStore keyStore) {
        try {
            return Collections.list(keyStore.aliases()).stream();
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
    }
}
