package internal.sdmxdl.cli;

import demetra.timeseries.Ts;
import demetra.timeseries.TsCollection;
import demetra.timeseries.TsData;
import demetra.timeseries.TsUnit;
import demetra.timeseries.util.ObsGathering;
import demetra.timeseries.util.TsDataBuilder;
import demetra.tsprovider.grid.GridDataType;
import demetra.tsprovider.grid.GridOutput;
import demetra.tsprovider.grid.GridWriter;
import demetra.tsprovider.util.ObsFormat;
import nbbrd.picocsv.Csv;
import sdmxdl.Frequency;
import sdmxdl.Obs;
import sdmxdl.Series;
import sdmxdl.web.SdmxWebConnection;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static internal.sdmxdl.cli.WebFlowOptions.SERIES_BY_KEY;

@lombok.experimental.UtilityClass
public class TsToolkitUtil {

    public ObsFormat toObsFormat(ObsFormatOptions opts, boolean hasTime) {
        return ObsFormat
                .builder()
                .locale(opts.getLocale())
                .dateTimePattern(hasTime ? opts.getDatetimePattern() : opts.getDatePattern())
                .numberPattern(opts.getNumberPattern())
                .ignoreNumberGrouping(opts.isIgnoreNumberGrouping())
                .build();
    }

    public TsCollection getSortedData(WebKeyOptions web, String titleAttribute) throws IOException {
        try (SdmxWebConnection conn = web.getManager().getConnection(web.getSource())) {
            try (Stream<Series> stream = conn.getDataStream(web.getFlow(), web.getKey(), web.getFilter())) {
                return stream
                        .sorted(SERIES_BY_KEY)
                        .map(series -> getTs(series, titleAttribute))
                        .collect(TO_TS_COLLECTION);
            }
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

    public static void writeGrid(Csv.Writer w, WebKeyOptions web, ObsFormatOptions format, boolean cornerFieldRequired, LayoutOptions layout) throws IOException {
        TsCollection data = TsToolkitUtil.getSortedData(web, layout.getTitleAttribute());
        GridWriter
                .builder()
                .format(TsToolkitUtil.toObsFormat(format, hasTime(data)))
                .cornerLabel("Period")
                .reverseChronology(layout.isReverseChronology())
                .build()
                .write(data, new CsvGridOutput(w));
    }

    private static boolean hasTime(TsCollection col) {
        return col.getData().stream().map(ts -> ts.getData().getTsUnit()).anyMatch(TsToolkitUtil::hasTime);
    }

    private static boolean hasTime(TsUnit unit) {
        return unit.getChronoUnit().isTimeBased();
    }

    @lombok.RequiredArgsConstructor
    private static final class CsvGridOutput implements GridOutput {

        @lombok.NonNull
        private final Csv.Writer csvWriter;

        @Override
        public Set<GridDataType> getDataTypes() {
            return EnumSet.of(GridDataType.STRING);
        }

        @Override
        public Stream open(String name, int rows, int columns) {
            return new CsvGridOutputStream(csvWriter);
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class CsvGridOutputStream implements GridOutput.Stream {

        @lombok.NonNull
        private final Csv.Writer csvWriter;

        @Override
        public void writeCell(Object value) throws IOException {
            csvWriter.writeField((CharSequence) value);
        }

        @Override
        public void writeEndOfRow() throws IOException {
            csvWriter.writeEndOfLine();
        }

        @Override
        public void close() {
            // do not close here
        }
    }
}

