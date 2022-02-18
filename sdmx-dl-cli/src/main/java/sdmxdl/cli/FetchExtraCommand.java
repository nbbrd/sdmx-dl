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
import sdmxdl.*;
import sdmxdl.web.SdmxWebConnection;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static internal.sdmxdl.cli.WebFlowOptions.SERIES_BY_KEY;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "extra", description = "Download and infer time series characteristics", hidden = true)
@SuppressWarnings("FieldMayBeFinal")
public final class FetchExtraCommand implements Callable<Void> {

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

    private CsvTable<Extra> getTable() {
        return CsvTable
                .builderOf(Extra.class)
                .columnOf("Series", Extra::getKey, Formatter.onObjectToString())
                .columnOf("TimeUnit", Extra::getTimeUnit, Formatter.onEnum())
                .columnOf("ValueUnit", Extra::getValueUnit, Formatter.onString())
                .columnOf("Decimals", Extra::getDecimals, Formatter.onString())
                .columnOf("Name", Extra::getName, Formatter.onString())
                .columnOf("Description", Extra::getDescription, Formatter.onString())
                .build();
    }

    private Stream<Extra> getRows() throws IOException {
        try (SdmxWebConnection conn = web.open(web.loadManager())) {
            DataStructure dsd = conn.getStructure(web.getFlow());

            Function<Series, String> toValueUnit = getValueUnit(dsd);
            Function<Series, String> toDecimal = getDecimal(dsd);
            Function<Series, String> toName = getName(dsd);
            Function<Series, String> toDescription = getDescription(dsd);

            return sort.applySort(conn.getData(web.getFlow(), DataQuery.of(web.getKey(), getDetail())).getData(), SERIES_BY_KEY)
                    .map(series -> new Extra(
                            series.getKey(),
                            series.getFreq(),
                            toValueUnit.apply(series),
                            toDecimal.apply(series),
                            toName.apply(series),
                            toDescription.apply(series)
                    ));
        }
    }

    private DataDetail getDetail() {
        return DataDetail.NO_DATA;
    }

    @lombok.Value
    private static class Extra {
        Key key;
        Frequency timeUnit;
        String valueUnit;
        String decimals;
        String name;
        String description;
    }

    private static Function<Series, String> getValueUnit(DataStructure dsd) {
        Dimension dimension = first(dsd.getDimensions(), o -> o.getId().contains("UNIT") && !o.getId().contains("MULT"), BY_LENGTH_ID);
        if (dimension != null) {
            return onDimension(dsd.getDimensionList().indexOf(dimension));
        }
        Attribute attribute = first(dsd.getAttributes(), o -> o.getId().contains("UNIT") && !o.getId().contains("MULT"), BY_LENGTH_ID);
        if (attribute != null) {
            return onAttribute(attribute);
        }
        return NOT_FOUND;
    }

    private static Function<Series, String> getDecimal(DataStructure dsd) {
        Attribute attribute = first(dsd.getAttributes(), o -> o.getId().contains("DECIMALS"), BY_LENGTH_ID);
        return attribute != null ? onAttribute(attribute) : NOT_FOUND;
    }

    private static Function<Series, String> getName(DataStructure dsd) {
        Attribute attribute = first(dsd.getAttributes(), o -> !o.isCoded() && o.getId().contains("TITLE"), BY_LENGTH_ID);
        return attribute != null ? onAttribute(attribute) : NOT_FOUND;
    }

    private static Function<Series, String> getDescription(DataStructure dsd) {
        Attribute attribute = first(dsd.getAttributes(), o -> !o.isCoded() && o.getId().contains("TITLE"), BY_LENGTH_ID.reversed());
        return attribute != null ? onAttribute(attribute) : NOT_FOUND;
    }

    private static Function<Series, String> NOT_FOUND = series -> "";

    private static Function<Series, String> onDimension(int dimensionIndex) {
        return series -> series.getKey().get(dimensionIndex);
    }

    private static Function<Series, String> onAttribute(Attribute component) {
        return series -> series.getMeta().get(component.getId());
    }

    private static <T> T first(Collection<T> list, Predicate<? super T> filter, Comparator<? super T> sorter) {
        return list.stream().filter(filter).sorted(sorter).findAny().orElse(null);
    }

    private static final Comparator<Component> BY_LENGTH_ID
            = Comparator.<Component>comparingInt(o -> o.getId().length()).thenComparing(Component::getId);
}
