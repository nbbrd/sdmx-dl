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
import picocli.CommandLine;
import sdmxdl.cli.protobuf.ContextDto;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "context", description = {"Print system and environment context."})
@SuppressWarnings("FieldMayBeFinal")
public final class DebugContextCommand implements Callable<Void> {

    @CommandLine.ArgGroup(validate = false, headingKey = "debug")
    private DebugOutputOptions output = new DebugOutputOptions();

    @CommandLine.Option(
            names = {"-t", "--type"},
            paramLabel = "<type>",
            description = {"Context type (${COMPLETION-CANDIDATES})."},
            defaultValue = "SYS"
    )
    private ContextType type;

    public Void call() throws Exception {
        output.dumpAll(ContextDto
                .newBuilder()
                .putAllItems(this.type.get())
                .build()
        );
        return null;
    }

    public enum ContextType implements Supplier<Map<String, String>> {
        SYS {
            public Map<String, String> get() {
                TreeMap<String, String> result = new TreeMap<>();
                System.getProperties().forEach((k, v) -> result.put(k.toString(), v.toString()));
                return result;
            }
        },
        ENV {
            public Map<String, String> get() {
                return new TreeMap<>(System.getenv());
            }
        };
    }
}
