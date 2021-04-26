package sdmxdl.csv;

import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataflowRef;
import sdmxdl.Frequency;
import sdmxdl.Series;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Locale;

/**
 * https://sdmx.org/wp-content/uploads/SDMX-CSV_format_specifications.docx
 */
@lombok.experimental.UtilityClass
public class SdmxCsvFields {

    public static final String DATAFLOW = "DATAFLOW";
    public static final String KEY_DIMENSIONS = "KEY_DIMENSIONS";
    public static final String TIME_DIMENSION = "TIME_DIMENSION";
    public static final String OBS_VALUE = "OBS_VALUE";
    public static final String ATTRIBUTES = "ATTRIBUTES";
    public static final String SERIESKEY = "SERIESKEY";

    public @NonNull Formatter<DataflowRef> getDataflowRefFormatter() {
        return SdmxCsvFields::formatDataflowField;
    }

    public @NonNull Parser<DataflowRef> getDataflowRefParser() {
        return SdmxCsvFields::parseDataflowField;
    }

    public @NonNull Formatter<Number> getValueFormatter(@NonNull Locale locale) {
        return Formatter.onNumberFormat(getNumberFormat(locale));
    }

    public @NonNull Formatter<LocalDateTime> getPeriodFormatter(@NonNull Collection<Series> data) {
        return Formatter.onDateTimeFormatter(getDateTimeFormatter(Frequency.getHighest(data)));
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

    private static String formatDataflowField(DataflowRef ref) {
        return ref != null ? (ref.getAgency() + ":" + ref.getId() + "(" + ref.getVersion() + ")") : null;
    }

    private static DataflowRef parseDataflowField(CharSequence ref) {
        if (ref == null) return null;
        String text = ref.toString();

        int idx1 = text.indexOf(':');
        if (idx1 == -1) return null;

        int idx2 = text.indexOf('(', idx1);
        if (idx2 == -1) return null;

        int idx3 = text.indexOf(')', idx2);
        if (idx3 == -1) return null;

        if (idx3 != text.length() - 1) return null;

        return DataflowRef.of(
                text.substring(0, idx1),
                text.substring(idx1 + 1, idx2),
                text.substring(idx2 + 1, idx3)
        );
    }
}
