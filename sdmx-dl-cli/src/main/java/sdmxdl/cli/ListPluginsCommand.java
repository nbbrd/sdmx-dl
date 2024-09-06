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

import internal.sdmxdl.cli.WebOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.CsvUtil;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import lombok.NonNull;
import picocli.CommandLine;
import sdmxdl.ext.Persistence;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.spi.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import static java.util.Collections.emptyList;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "plugins")
public final class ListPluginsCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @Override
    public Void call() throws Exception {
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<Plugin> getTable() {
        return CsvTable
                .builderOf(Plugin.class)
                .columnOf("Type", Plugin::getType)
                .columnOf("Id", Plugin::getId)
                .columnOf("Properties", Plugin::getProperties, CsvUtil.DEFAULT_LIST_FORMATTER)
                .build();
    }

    private List<Plugin> getRows() throws IOException {
        SdmxWebManager manager = web.loadManager();
        List<Plugin> result = new ArrayList<>();
        manager.getDrivers().stream().map(Plugin::of).forEach(result::add);
        manager.getAuthenticators().stream().map(Plugin::of).forEach(result::add);
        manager.getMonitors().stream().map(Plugin::of).forEach(result::add);
        manager.getPersistences().stream().map(Plugin::of).forEach(result::add);
        result.add(Plugin.of(manager.getRegistry()));
        result.add(Plugin.of(manager.getCaching()));
        result.add(Plugin.of(manager.getNetworking()));
        return result;
    }

    @lombok.Value
    private static class Plugin {

        static Plugin of(Driver o) {
            return new Plugin("Driver", o.getDriverId(), o.getDriverProperties());
        }

        static Plugin of(Authenticator o) {
            return new Plugin("Authenticator", o.getAuthenticatorId(), emptyList());
        }

        static Plugin of(Monitor o) {
            return new Plugin("Monitor", o.getMonitorId(), emptyList());
        }

        static Plugin of(Persistence o) {
            return new Plugin("Persistence", o.getPersistenceId(), emptyList());
        }

        static Plugin of(Registry o) {
            return new Plugin("Registry", o.getRegistryId(), o.getRegistryProperties());
        }

        static Plugin of(WebCaching o) {
            return new Plugin("WebCaching", o.getWebCachingId(), o.getWebCachingProperties());
        }

        static Plugin of(FileCaching o) {
            return new Plugin("FileCaching", o.getFileCachingId(), o.getFileCachingProperties());
        }

        static Plugin of(Networking o) {
            return new Plugin("Networking", o.getNetworkingId(), o.getNetworkingProperties());
        }

        @NonNull
        String type;

        @NonNull
        String id;

        @NonNull
        Collection<String> Properties;
    }
}
