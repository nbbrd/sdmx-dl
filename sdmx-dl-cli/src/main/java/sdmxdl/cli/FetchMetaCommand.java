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

import internal.sdmxdl.cli.SortOptions;
import internal.sdmxdl.cli.WebKeyOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import nbbrd.io.text.Formatter;
import picocli.CommandLine;
import sdmxdl.Key;

import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static sdmxdl.Detail.NO_DATA;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "meta")
@SuppressWarnings("FieldMayBeFinal")
public final class FetchMetaCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebKeyOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Mixin
    private SortOptions sort;

    @Override
    public Void call() throws Exception {
        getTable().write(csv, getRows());
        return null;
    }

    private CsvTable<MetaResult> getTable() {
        return CsvTable
                .builderOf(MetaResult.class)
                .columnOf("Series", MetaResult::getKey, Formatter.onObjectToString())
                .columnOf("Concept", MetaResult::getConcept)
                .columnOf("Value", MetaResult::getValue)
                .build();
    }

    private Stream<MetaResult> getRows() throws IOException {
        return sort.applySort(
                web.loadManager().using(web.getSource()).getData(web.toKeyRequest(NO_DATA)).getData().stream().flatMap(this::getMetaResultStream),
                BY_FLOW_KEY_CONCEPT
        );
    }

    private Stream<MetaResult> getMetaResultStream(sdmxdl.Series series) {
        return series.getMeta().entrySet().stream().map(o -> new MetaResult(series.getKey(), o.getKey(), o.getValue()));
    }

    @lombok.Value
    private static class MetaResult {
        Key key;
        String concept;
        String value;
    }

    private static final Comparator<MetaResult> BY_FLOW_KEY_CONCEPT = Comparator
            .comparing((MetaResult o) -> o.getKey().toString())
            .thenComparing(MetaResult::getConcept);
}
