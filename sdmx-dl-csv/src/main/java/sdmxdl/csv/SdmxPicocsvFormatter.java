package sdmxdl.csv;

import nbbrd.io.text.Formatter;
import nbbrd.io.text.TextFormatter;
import nbbrd.picocsv.Csv;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.repo.DataSet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

@lombok.Builder(toBuilder = true)
public final class SdmxPicocsvFormatter implements TextFormatter<DataSet> {

    @lombok.NonNull
    private final DataStructure dsd;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Csv.Format format = Csv.Format.RFC4180;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Function<DataSet, Formatter<LocalDateTime>> periodFormat = SdmxPicocsvFormatter::getPeriodFormatter;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Function<DataSet, Formatter<Number>> valueFormat = SdmxPicocsvFormatter::getValueFormatter;

    @lombok.Builder.Default
    private final List<SdmxCsvField> fields = Arrays.asList(SdmxCsvField.values());

    @lombok.Builder.Default
    private final boolean ignoreHeader = false;

    @Override
    public void formatWriter(@NonNull DataSet data, @NonNull Writer writer) throws IOException {
        try (Csv.Writer csv = Csv.Writer.of(writer, format)) {
            format(data, csv);
        }
    }

    @Override
    public void formatStream(@NonNull DataSet data, @NonNull OutputStream outputStream, @NonNull Charset charset) throws IOException {
        try (Csv.Writer csv = Csv.Writer.of(outputStream, charset, format)) {
            format(data, csv);
        }
    }

    public void format(@NonNull DataSet data, Csv.@NonNull Writer w) throws IOException {
        if (!ignoreHeader) {
            for (SdmxCsvField field : fields) {
                switch (field) {
                    case DATAFLOW:
                        w.writeField(SdmxCsv.DATAFLOW);
                        break;
                    case KEY_DIMENSIONS:
                        for (Dimension dimension : getSortedDimensions(dsd)) {
                            w.writeField(dimension.getId());
                        }
                        break;
                    case TIME_DIMENSION:
                        w.writeField(dsd.getTimeDimensionId());
                        break;
                    case OBS_VALUE:
                        w.writeField(SdmxCsv.OBS_VALUE);
                        break;
                    case SERIESKEY:
                        w.writeField(SdmxCsv.SERIESKEY);
                        break;
                }
            }
            w.writeEndOfLine();
        }

        String dataflow = SdmxCsv.getDataflowRefFormatter().formatAsString(data.getRef());
        Formatter<LocalDateTime> periodFormatter = periodFormat.apply(data);
        Formatter<Number> valueFormatter = valueFormat.apply(data);

        for (Series series : data.getData()) {
            String key = series.getKey().toString();
            for (Obs obs : series.getObs()) {
                for (SdmxCsvField field : fields) {
                    switch (field) {
                        case DATAFLOW:
                            w.writeField(dataflow);
                            break;
                        case KEY_DIMENSIONS:
                            for (int i = 0; i < series.getKey().size(); i++) {
                                w.writeField(series.getKey().get(i));
                            }
                            break;
                        case TIME_DIMENSION:
                            w.writeField(periodFormatter.format(obs.getPeriod()));
                            break;
                        case OBS_VALUE:
                            w.writeField(valueFormatter.format(obs.getValue()));
                            break;
                        case SERIESKEY:
                            w.writeField(key);
                            break;
                    }
                }
                w.writeEndOfLine();
            }
        }
    }

    private static Formatter<Number> getValueFormatter(DataSet data) {
        return Formatter.onNumberFormat(getNumberFormat(Locale.ROOT));
    }

    private static Formatter<LocalDateTime> getPeriodFormatter(DataSet data) {
        return Formatter.onDateTimeFormatter(getDateTimeFormatter(Frequency.getHighest(data.getData())));
    }

    private static NumberFormat getNumberFormat(Locale encoding) {
        DecimalFormat decimalFormat = new DecimalFormat("", DecimalFormatSymbols.getInstance(encoding));
        decimalFormat.setGroupingUsed(false);
        return decimalFormat;
    }

    private static DateTimeFormatter getDateTimeFormatter(Frequency freq) {
        switch (freq) {
            case ANNUAL:
                return DateTimeFormatter.ofPattern("yyyy");
            case HALF_YEARLY:
            case QUARTERLY:
            case MONTHLY:
                return DateTimeFormatter.ofPattern("yyyy-MM");
            case WEEKLY:
            case DAILY:
            case DAILY_BUSINESS:
                return DateTimeFormatter.ofPattern("yyyy-MM-dd");
            case HOURLY:
                return DateTimeFormatter.ofPattern("yyyy-MM-ddTHH");
            case MINUTELY:
                return DateTimeFormatter.ofPattern("yyyy-MM-ddTHH:mm");
            case UNDEFINED:
                return DateTimeFormatter.ISO_DATE_TIME;
            default:
                throw new RuntimeException();
        }
    }

    private static SortedSet<Dimension> getSortedDimensions(DataStructure dsd) {
        return sortedCopyOf(dsd.getDimensions(), Comparator.comparingInt(Dimension::getPosition));
    }

    private static <T> SortedSet<T> sortedCopyOf(Set<T> origin, Comparator<T> comparator) {
        TreeSet<T> result = new TreeSet<>(comparator);
        result.addAll(origin);
        return result;
    }
}
