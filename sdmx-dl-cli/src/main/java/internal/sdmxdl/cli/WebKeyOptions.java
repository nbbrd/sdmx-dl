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

import demetra.timeseries.Ts;
import demetra.timeseries.TsCollection;
import demetra.timeseries.TsData;
import demetra.timeseries.TsUnit;
import demetra.timeseries.util.ObsGathering;
import demetra.timeseries.util.TsDataBuilder;
import picocli.CommandLine;
import sdmxdl.*;
import sdmxdl.web.SdmxWebConnection;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
@lombok.Data
public class WebKeyOptions extends WebFlowOptions {

    @CommandLine.Parameters(
            index = "2",
            paramLabel = "<key>",
            converter = KeyConverter.class,
            descriptionKey = "sdmxdl.cli.key"
    )
    private Key key;

    public TsCollection getData(String titleAttribute) throws IOException {
        DataFilter filter = DataFilter.ALL;
        try (SdmxWebConnection conn = getManager().getConnection(getSource())) {
            try (Stream<Series> stream = conn.getDataStream(getFlow(), getKey(), filter)) {
                return stream
                        .sorted(BY_KEY)
                        .map(series -> getTs(series, titleAttribute))
                        .collect(TO_TS_COLLECTION);
            }
        }
    }

    public List<Series> getSeries() throws IOException {
        DataFilter filter = DataFilter.ALL;
        try (SdmxWebConnection conn = getManager().getConnection(getSource())) {
            return conn.getData(getFlow(), getKey(), filter);
        }
    }

    private Ts getTs(Series series, String titleAttribute) {
        Ts.Builder result = Ts.builder();
        result.name(getTitle(series, titleAttribute));
        result.data(getTsData(series));
        return result.build();
    }

    private static String getTitle(Series series, String titleAttribute) {
        if (titleAttribute != null && !titleAttribute.isEmpty()) {
            String result = series.getMeta().get(titleAttribute);
            if (result != null) {
                return result;
            }
        }
        return series.getKey().toString();
    }

    private static TsData getTsData(Series series) {
        return TsDataBuilder
                .byDateTime(ObsGathering.DEFAULT.withUnit(getUnit(series.getFreq())))
                .addAll(series.getObs().stream(), Obs::getPeriod, Obs::getValue)
                .build();
    }

    private static TsUnit getUnit(Frequency freq) {
        switch (freq) {
            case ANNUAL:
                return TsUnit.YEAR;
            case DAILY:
                return TsUnit.DAY;
            case DAILY_BUSINESS:
                return TsUnit.DAY;
            case HALF_YEARLY:
                return TsUnit.HALF_YEAR;
            case HOURLY:
                return TsUnit.HOUR;
            case MINUTELY:
                return TsUnit.MINUTE;
            case MONTHLY:
                return TsUnit.MONTH;
            case QUARTERLY:
                return TsUnit.QUARTER;
            case UNDEFINED:
                return TsUnit.UNDEFINED;
            case WEEKLY:
                return TsUnit.WEEK;
            default:
                throw new RuntimeException();
        }
    }

    private static final Collector<Ts, TsCollection.Builder, TsCollection> TO_TS_COLLECTION
            = Collector.of(TsCollection::builder, TsCollection.Builder::data, (l, r) -> l.data(r.getData()), TsCollection.Builder::build);
}
