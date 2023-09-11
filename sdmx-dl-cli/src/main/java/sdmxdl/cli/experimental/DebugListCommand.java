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
import internal.sdmxdl.cli.WebFlowOptions;
import internal.sdmxdl.cli.WebOptions;
import internal.sdmxdl.cli.WebSourceOptions;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Spec;
import sdmxdl.Detail;
import sdmxdl.Flow;
import sdmxdl.Feature;
import sdmxdl.Key;
import sdmxdl.cli.protobuf.Features;
import sdmxdl.cli.protobuf.Flows;
import sdmxdl.cli.protobuf.Sources;
import sdmxdl.format.protobuf.ProtobufRepositories;
import sdmxdl.format.protobuf.ProtobufSources;
import sdmxdl.web.WebSource;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@Command(name = "list", description = "Print raw resources")
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
        nonNull(out).dumpAll(fromWebSources(web.loadManager().getSources().values()));
    }

    private static Sources fromWebSources(Collection<WebSource> value) {
        return Sources
                .newBuilder()
                .addAllSources(value.stream().map(ProtobufSources::fromWebSource).collect(Collectors.toList()))
                .build();
    }

    @Command
    public void flows(@Mixin WebSourceOptions web, @ArgGroup(validate = false, headingKey = "debug") DebugOutputOptions out) throws Exception {
        nonNull(out).dumpAll(fromDataflows(web.loadFlows(web.loadManager(), web.getLangs())));
    }

    private static Flows fromDataflows(Collection<Flow> value) {
        return Flows
                .newBuilder()
                .addAllFlows(value.stream().map(ProtobufRepositories::fromDataflow).collect(Collectors.toList()))
                .build();
    }

    @Command
    public void keys(@Mixin WebFlowOptions web, @ArgGroup(validate = false, headingKey = "debug") DebugOutputOptions out) throws Exception {
        nonNull(out).dumpAll(ProtobufRepositories.fromDataSet(web.loadSeries(web.loadManager(), Key.ALL, Detail.SERIES_KEYS_ONLY)));
    }

    @Command
    public void features(@Mixin WebSourceOptions web, @ArgGroup(validate = false, headingKey = "debug") DebugOutputOptions out) throws Exception {
        nonNull(out).dumpAll(fromFeatures(web.loadFeatures(web.loadManager(), web.getLangs())));
    }

    private static Features fromFeatures(Collection<Feature> value) {
        return Features
                .newBuilder()
                .addAllFeatures(value.stream().map(ProtobufRepositories::fromFeature).collect(Collectors.toList()))
                .build();
    }

    private DebugOutputOptions nonNull(DebugOutputOptions options) {
        return options != null ? options : new DebugOutputOptions();
    }
}
