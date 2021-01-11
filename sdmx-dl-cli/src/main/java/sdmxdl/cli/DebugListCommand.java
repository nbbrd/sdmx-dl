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
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Spec;
import sdmxdl.Dataflow;
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
        spec.commandLine().usage(System.out);
        return null;
    }

    @Command(sortOptions = false, mixinStandardHelpOptions = true)
    public void sources(@Mixin WebOptions web, @Mixin DebugOutputOptions out) throws Exception {
        out.dumpAll(SdmxWebSource.class, web.getManager().getSources().values());
    }

    @Command(sortOptions = false, mixinStandardHelpOptions = true)
    public void flows(@Mixin WebSourceOptions web, @Mixin DebugOutputOptions out) throws Exception {
        out.dumpAll(Dataflow.class, web.getSortedFlows());
    }

    @Command(sortOptions = false, mixinStandardHelpOptions = true)
    public void keys(@Mixin WebFlowOptions web, @Mixin DebugOutputOptions out) throws Exception {
        out.dumpAll(Series.class, web.getSortedSeriesKeys());
    }

    @Command(sortOptions = false, mixinStandardHelpOptions = true)
    public void features(@Mixin WebSourceOptions web, @Mixin DebugOutputOptions out) throws Exception {
        out.dumpAll(Feature.class, web.getSortedFeatures());
    }
}
