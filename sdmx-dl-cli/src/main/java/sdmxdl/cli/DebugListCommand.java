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

import internal.sdmxdl.cli.*;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Spec;
import sdmxdl.DataFilter;
import sdmxdl.Dataflow;
import sdmxdl.Key;
import sdmxdl.Series;
import sdmxdl.web.SdmxWebSource;

import java.util.concurrent.Callable;

/**
 * @author Philippe Charles
 */
@Command(
        name = "list",
        description = "List resources of a remote SDMX server."
)
public final class DebugListCommand implements Callable<Void> {

    @Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Void call() {
        spec.commandLine().usage(spec.commandLine().getOut());
        return null;
    }

    @Command
    public void sources(@Mixin WebOptions web, @ArgGroup(validate = false, headingKey = "debug") DebugOutputOptions out) throws Exception {
        nonNull(out).dumpAll(SdmxWebSource.class, web.loadManager().getSources().values());
    }

    @Command
    public void flows(@Mixin WebSourceOptions web, @ArgGroup(validate = false, headingKey = "debug") DebugOutputOptions out) throws Exception {
        nonNull(out).dumpAll(Dataflow.class, web.loadFlows(web.loadManager()));
    }

    @Command
    public void keys(@Mixin WebFlowOptions web, @ArgGroup(validate = false, headingKey = "debug") DebugOutputOptions out) throws Exception {
        nonNull(out).dumpAll(Series.class, web.loadSeries(web.loadManager(), Key.ALL, DataFilter.SERIES_KEYS_ONLY));
    }

    @Command
    public void features(@Mixin WebSourceOptions web, @ArgGroup(validate = false, headingKey = "debug") DebugOutputOptions out) throws Exception {
        nonNull(out).dumpAll(Feature.class, web.loadFeatures(web.loadManager()));
    }

    private DebugOutputOptions nonNull(DebugOutputOptions options) {
        return options != null ? options : new DebugOutputOptions();
    }
}
