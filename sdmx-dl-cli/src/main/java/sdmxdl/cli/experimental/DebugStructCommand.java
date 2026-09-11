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
import java.util.concurrent.Callable;
import picocli.CommandLine;
import sdmxdl.MetaRequest;
import sdmxdl.format.protobuf.ProtoApi;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "struct", description = "Print raw struct")
@SuppressWarnings("FieldMayBeFinal")
public final class DebugStructCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebFlowOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "debug")
    private DebugOutputOptions output = new DebugOutputOptions();

    @Override
    public Void call() throws Exception {
        output.dumpAll(ProtoApi.fromDataStructure(web.loadManager()
                .usingName(web.getSource())
                .getMeta(MetaRequest.builder()
                        .languages(web.getLangs())
                        .database(web.getDatabase())
                        .flow(web.getFlow())
                        .build())
                .getStructure()));
        return null;
    }
}
