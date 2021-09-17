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
package sdmxdl.cli;

import internal.sdmxdl.cli.Excel;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.VerboseOptions;
import nbbrd.console.picocli.ConfigHelper;
import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;
import sdmxdl.About;

import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "config")
@SuppressWarnings("FieldMayBeFinal")
public final class CheckConfigCommand implements Callable<Void> {

    @CommandLine.Mixin
    private VerboseOptions verboseOptions;

    @CommandLine.ArgGroup(validate = false, headingKey = "csv")
    private final CsvOutputOptions csv = new CsvOutputOptions();

    @CommandLine.Mixin
    private Excel excel;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() throws Exception {
        excel.apply(csv);
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<ScopedProperty> getTable() {
        return CsvTable
                .builderOf(ScopedProperty.class)
                .columnOf("Scope", ScopedProperty::getScope, Formatter.onEnum())
                .columnOf("PropertyKey", ScopedProperty::getKey, Formatter.onString())
                .columnOf("PropertyValue", ScopedProperty::getValue, Formatter.onString())
                .columnOf("Category", ScopedProperty::getCategory, Formatter.onEnum())
                .build();
    }

    private Stream<ScopedProperty> getRows() {
        return getProperties(getConfigHelper())
                .entrySet()
                .stream()
                .flatMap(entry -> getEntries(entry.getKey(), entry.getValue(), spec.commandLine()));
    }

    private ConfigHelper getConfigHelper() {
        return ConfigHelper
                .builder()
                .appName(About.NAME)
                .onLoadingError((path, ex) -> verboseOptions.reportToErrorStream("CONFIG", path.toString(), ex))
                .build();
    }

    private static SortedMap<ConfigHelper.Scope, Properties> getProperties(ConfigHelper helper) {
        SortedMap<ConfigHelper.Scope, Properties> result = new TreeMap<>();
        for (ConfigHelper.Scope scope : ConfigHelper.Scope.values()) {
            Properties properties = new Properties();
            helper.load(properties, scope);
            result.put(scope, properties);
        }
        return result;
    }

    private static Stream<ScopedProperty> getEntries(ConfigHelper.Scope scope, Properties properties, CommandLine cmd) {
        return properties.stringPropertyNames().stream()
                .map(key -> new ScopedProperty(scope, key, properties.getProperty(key), Category.get(key, getRoot(cmd))));
    }

    private static CommandLine getRoot(CommandLine cmd) {
        CommandLine parent = cmd.getParent();
        return parent != null ? getRoot(parent) : cmd;
    }

    private enum Category {

        WIDE_OPTION, NARROW_OPTION, OTHER;

        static Category get(String key, CommandLine cmd) {
            for (CommandLine.Model.OptionSpec option : cmd.getCommandSpec().options()) {
                String descriptionKey = option.descriptionKey();
                if (descriptionKey != null) {
                    if (key.equals(cmd.getCommandSpec().qualifiedName(".") + "." + descriptionKey))
                        return NARROW_OPTION;
                    if (key.equals(descriptionKey)) return WIDE_OPTION;
                }
                String longestName = stripPrefix(option.longestName());
                if (key.equals(cmd.getCommandSpec().qualifiedName(".") + "." + longestName)) return NARROW_OPTION;
                if (key.equals(longestName)) return WIDE_OPTION;
            }
            for (CommandLine sub : cmd.getSubcommands().values()) {
                Category result = get(key, sub);
                if (result != OTHER) {
                    return result;
                }
            }
            return OTHER;
        }

        private static String stripPrefix(String prefixed) {
            for (int i = 0; i < prefixed.length(); i++) {
                if (Character.isJavaIdentifierPart(prefixed.charAt(i))) {
                    return prefixed.substring(i);
                }
            }
            return prefixed;
        }
    }

    @lombok.Value
    private static class ScopedProperty {
        ConfigHelper.Scope scope;
        String key;
        String value;
        Category category;
    }
}
