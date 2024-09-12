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

import internal.sdmxdl.cli.Plugin;
import internal.sdmxdl.cli.WebSourcesOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import picocli.CommandLine;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "sources")
@SuppressWarnings("FieldMayBeFinal")
public final class CheckSourcesCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebSourcesOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @Override
    public Void call() throws Exception {
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<SourceIssue> getTable() {
        return CsvTable
                .builderOf(SourceIssue.class)
                .columnOf("ID", SourceIssue::getId)
                .columnOf("Issue", SourceIssue::getIssue)
                .build();
    }

    private Stream<SourceIssue> getRows() throws IOException {
        SdmxWebManager manager = web.loadManager();
        Stream<String> sources = web.isAllSources() ? WebSourcesOptions.getAllSourceNames(manager) : web.getSources().stream();
        List<Plugin> plugins = Plugin.allOf(manager);
        return web.applyParallel(sources).map(source -> check(source, manager, plugins));
    }

    private SourceIssue check(String sourceID, SdmxWebManager manager, List<Plugin> plugins) {
        WebSource webSource = manager.getSources().get(sourceID);
        if (webSource == null) {
            return new SourceIssue(sourceID, "Source not found");
        }
        Optional<Driver> driver = manager.getDrivers().stream().filter(o -> o.getDriverId().equals(webSource.getDriver())).findFirst();
        if (!driver.isPresent()) {
            return new SourceIssue(sourceID, "Driver not found");
        }
        Collection<String> expected = plugins.stream()
                .filter(plugin -> !plugin.getType().equals(Plugin.Type.DRIVER) || plugin.getId().equals(driver.get().getDriverId()))
                .map(Plugin::getProperties)
                .flatMap(Collection::stream)
                .collect(toList());
        Collection<String> found = webSource.getProperties().keySet();
        String result = found.stream().filter(item -> !expected.contains(item)).sorted().collect(Collectors.joining(","));
        return new SourceIssue(sourceID, result.isEmpty() ? "No problem" : ("Unknown properties: " + result));
    }

    @lombok.Value
    private static class SourceIssue {
        String id;
        String issue;
    }
}
