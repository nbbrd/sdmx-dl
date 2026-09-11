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
import internal.sdmxdl.cli.WebKeyOptions;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import sdmxdl.DataRequest;
import sdmxdl.Detail;
import sdmxdl.format.protobuf.ProtoApi;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "data", description = "Print raw data")
@SuppressWarnings("FieldMayBeFinal")
public final class DebugDataCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebKeyOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "debug")
    private DebugOutputOptions output = new DebugOutputOptions();

    @Override
    public Void call() throws Exception {
        output.dumpAll(ProtoApi.fromDataSet(web.loadManager()
                .usingName(web.getSource())
                .getData(DataRequest.builder()
                        .languages(web.getLangs())
                        .database(web.getDatabase())
                        .flow(web.getFlow())
                        .key(web.getKey())
                        .detail(Detail.FULL)
                        .build())));
        return null;
    }
}
