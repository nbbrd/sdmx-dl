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
package internal.sdmxdl.cli;

import picocli.CommandLine;
import sdmxdl.DatabaseRef;
import sdmxdl.DatabaseRequest;
import sdmxdl.Flow;
import sdmxdl.SourceRequest;

import java.util.Comparator;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public class WebSourceOptions extends WebNetOptions {

    @CommandLine.Parameters(
            index = "0",
            paramLabel = "<source>",
            descriptionKey = "cli.sdmx.source"
    )
    private String source;

    @CommandLine.Option(
            names = {"-d", "--database"},
            paramLabel = "<database>",
            converter = DatabaseRefConverter.class,
            descriptionKey = "cli.sdmx.database"
    )
    private DatabaseRef database;

    public DatabaseRef getDatabase() {
        return database == null ? DatabaseRef.NO_DATABASE : database;
    }

    public SourceRequest toSourceRequest() {
        return SourceRequest
                .builder()
                .languages(getLangs())
                .build();
    }

    public DatabaseRequest toDatabaseRequest() {
        return DatabaseRequest
                .builderOf(toSourceRequest())
                .database(getDatabase())
                .build();
    }

    public static final Comparator<Flow> FLOWS_BY_REF = Comparator.comparing(dataflow -> dataflow.getRef().toString());
}
