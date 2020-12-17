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

@lombok.Builder(toBuilder = true)
public final class SdmxPicocsvFormatter implements TextFormatter<DataSet> {

    @lombok.NonNull
    private final DataStructure dsd;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Csv.Format format = Csv.Format.RFC4180;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Locale encoding = Locale.ROOT;

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
        w.writeField(SdmxCsv.DATAFLOW);
        for (Dimension dimension : getSortedDimensions(dsd)) {
            w.writeField(dimension.getId());
        }
        w.writeField(dsd.getTimeDimensionId());
        w.writeField(SdmxCsv.OBS_VALUE);
        w.writeField(SdmxCsv.SERIESKEY);
        w.writeEndOfLine();

        String dataflow = toDataflowField(data.getRef());
        Formatter<LocalDateTime> periodFormatter = Formatter.onDateTimeFormatter(getDateTimeFormatter(Frequency.getHighest(data.getData())));
        Formatter<Number> valueFormatter = Formatter.onNumberFormat(getNumberFormat(encoding));

        for (Series series : data.getData()) {
            String key = series.getKey().toString();
            for (Obs obs : series.getObs()) {
                w.writeField(dataflow);
                for (int i = 0; i < series.getKey().size(); i++) {
                    w.writeField(series.getKey().get(i));
                }
                w.writeField(periodFormatter.format(obs.getPeriod()));
                w.writeField(valueFormatter.format(obs.getValue()));
                w.writeField(key);
                w.writeEndOfLine();
            }
        }
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
                return DateTimeFormatter.ofPattern("yyyy-MM");
            case QUARTERLY:
                return DateTimeFormatter.ofPattern("yyyy-MM");
            case MONTHLY:
                return DateTimeFormatter.ofPattern("yyyy-MM");
            case WEEKLY:
                return DateTimeFormatter.ofPattern("yyyy-MM-dd");
            case DAILY:
                return DateTimeFormatter.ofPattern("yyyy-MM-dd");
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

    private static String toDataflowField(DataflowRef ref) {
        return ref.getAgency() + ":" + ref.getId() + "(" + ref.getVersion() + ")";
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
