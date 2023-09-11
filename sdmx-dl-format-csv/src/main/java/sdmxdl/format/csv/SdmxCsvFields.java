package sdmxdl.format.csv;

import lombok.NonNull;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import sdmxdl.FlowRef;
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

    public @NonNull Formatter<FlowRef> getDataflowRefFormatter() {
        return SdmxCsvFields::formatDataflowField;
    }

    public @NonNull Parser<FlowRef> getDataflowRefParser() {
        return SdmxCsvFields::parseDataflowField;
    }

    public @NonNull Formatter<Number> getValueFormatter(@NonNull Locale locale) {
        return Formatter.onNumberFormat(getNumberFormat(locale));
    }

    public @NonNull Formatter<LocalDateTime> getPeriodFormatter(@NonNull Collection<Series> data) {
        return Formatter.onDateTimeFormatter(getDateTimeFormatter());
    }

    private static NumberFormat getNumberFormat(Locale encoding) {
        DecimalFormat decimalFormat = new DecimalFormat("", DecimalFormatSymbols.getInstance(encoding));
        decimalFormat.setGroupingUsed(false);
        return decimalFormat;
    }

    private static DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ISO_DATE_TIME;
    }

    private static String formatDataflowField(FlowRef ref) {
        return ref != null ? (ref.getAgency() + ":" + ref.getId() + "(" + ref.getVersion() + ")") : null;
    }

    private static FlowRef parseDataflowField(CharSequence ref) {
        if (ref == null) return null;
        String text = ref.toString();

        int idx1 = text.indexOf(':');
        if (idx1 == -1) return null;

        int idx2 = text.indexOf('(', idx1);
        if (idx2 == -1) return null;

        int idx3 = text.indexOf(')', idx2);
        if (idx3 == -1) return null;

        if (idx3 != text.length() - 1) return null;

        return FlowRef.of(
                text.substring(0, idx1),
                text.substring(idx1 + 1, idx2),
                text.substring(idx2 + 1, idx3)
        );
    }
}
