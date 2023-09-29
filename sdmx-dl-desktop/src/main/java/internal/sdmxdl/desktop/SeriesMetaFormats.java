package internal.sdmxdl.desktop;

import nbbrd.io.text.Parser;
import sdmxdl.provider.ext.SeriesMeta;

import java.text.*;
import java.time.temporal.TemporalAmount;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class SeriesMetaFormats {

    public static DateFormat getDateFormat(SeriesMeta meta) {
        TemporalAmount timeUnit = meta.getTimeUnit();
        if (timeUnit != null) {
            switch (timeUnit.toString()) {
                case "P1D":
                    return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault(Locale.Category.DISPLAY));
                case "P1M":
                    return new SimpleDateFormat("yyyy-MM", Locale.getDefault(Locale.Category.DISPLAY));
                case "P1Y":
                    return new SimpleDateFormat("yyyy", Locale.getDefault(Locale.Category.DISPLAY));
            }
        }
        return SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault(Locale.Category.DISPLAY));
    }

    public static NumberFormat getNumberFormat(SeriesMeta meta) {
        return Parser.onInteger()
                .parseValue(meta.getDecimals())
                .map(SeriesMetaFormats::getValuePattern)
                .map(pattern -> new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.DISPLAY))))
                .map(NumberFormat.class::cast)
                .orElseGet(() -> NumberFormat.getInstance(Locale.getDefault(Locale.Category.DISPLAY)));
    }

    private static String getValuePattern(int decimals) {
        return IntStream.range(0, decimals)
                .mapToObj(i -> "0")
                .collect(Collectors.joining("", "0.", ""));
    }
}
