package internal.sdmxdl.desktop;

import nbbrd.io.text.Parser;
import sdmxdl.ext.SeriesMeta;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class SeriesMetaFormats {

    public static DateTimeFormatter getDateTimeFormatter(SeriesMeta meta) {
        TemporalAmount timeUnit = meta.getTimeUnit();
        if (timeUnit != null) {
            switch (timeUnit.toString()) {
                case "P1D":
                    return DateTimeFormatter.ISO_LOCAL_DATE;
                case "P1M":
                    return DateTimeFormatter.ofPattern("uuuu-MM");
                case "P1Y":
                    return DateTimeFormatter.ofPattern("uuuu");
            }
        }
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    }

    public static DateFormat getDateFormat(SeriesMeta meta) {
        TemporalAmount timeUnit = meta.getTimeUnit();
        if (timeUnit != null) {
            switch (timeUnit.toString()) {
                case "P1D":
                    return new SimpleDateFormat("yyyy-MM-dd");
                case "P1M":
                    return new SimpleDateFormat("yyyy-MM");
                case "P1Y":
                    return new SimpleDateFormat("yyyy");
            }
        }
        return SimpleDateFormat.getDateTimeInstance();
    }

    public static NumberFormat getNumberFormat(SeriesMeta meta) {
        return Parser.onInteger()
                .parseValue(meta.getDecimals())
                .map(SeriesMetaFormats::getValuePattern)
                .map(DecimalFormat::new)
                .map(NumberFormat.class::cast)
                .orElseGet(NumberFormat::getInstance);
    }

    private static String getValuePattern(int decimals) {
        return IntStream.range(0, decimals)
                .mapToObj(i -> "0")
                .collect(Collectors.joining("", "0.", ""));
    }
}
