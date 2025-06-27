package sdmxdl;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import static java.time.temporal.ChronoField.NANO_OF_SECOND;

/**
 * Simplified implementation of <a href="https://en.wikipedia.org/wiki/ISO_8601#Time_intervals">ISO-8601 time intervals</a>
 * that uses the <code>start/duration</code> expression such as <code>2007-03-01T13:00:00Z/P1Y2M10DT2H30M</code>.
 */
@RepresentableAsString
@lombok.Value(staticConstructor = "of")
public class TimeInterval {

    @StaticFactoryMethod
    public static @NonNull TimeInterval parse(@NonNull CharSequence text) throws DateTimeParseException {
        String textAsString = text.toString();
        int index = textAsString.indexOf(SOLIDUS);
        if (index == -1) {
            throw new DateTimeParseException("Cannot parse time interval", text, 0);
        }
        return new TimeInterval(
                LocalDateTime.parse(textAsString.substring(0, index), AT_LEAST_YEAR),
                Duration.parse(textAsString.substring(index + 1))
        );
    }

    @NonNull
    LocalDateTime start;

    @NonNull
    Duration duration;

    @Override
    public String toString() {
        return toIsoString(start) + SOLIDUS + duration;
    }

    public @NonNull String toShortString() {
        return getStartAsShortString() + SOLIDUS + duration;
    }

    /**
     * Gets the start part of this time interval as a short string using
     * the <a href="https://en.wikipedia.org/wiki/ISO_8601#Reduced_precision">ISO_8601 reduced precision</a> mechanism.
     *
     * @return a non-null string
     */
    public @NonNull String getStartAsShortString() {
        if (start.getNano() != 0) {
            return start.toString();
        }

        int year = start.getYear();
        int month = start.getMonthValue();
        int day = start.getDayOfMonth();
        int hour = start.getHour();
        int minute = start.getMinute();
        int second = start.getSecond();

        ChronoUnit startUnit = getMinChronoUnit(second, minute, hour, day, month);
        ChronoUnit durationUnit = duration.getMinChronoUnit();

        ChronoUnit min = min(startUnit, durationUnit);

        switch (min) {
            case SECONDS:
                return year +
                        (month < 10 ? "-0" : "-") + month +
                        (day < 10 ? "-0" : "-") + day +
                        (hour < 10 ? "T0" : "T") + hour +
                        (minute < 10 ? ":0" : ":") + minute +
                        (second < 10 ? ":0" : ":") + second;
            case MINUTES:
                return year +
                        (month < 10 ? "-0" : "-") + month +
                        (day < 10 ? "-0" : "-") + day +
                        (hour < 10 ? "T0" : "T") + hour +
                        (minute < 10 ? ":0" : ":") + minute;
            case HOURS:
                return year +
                        (month < 10 ? "-0" : "-") + month +
                        (day < 10 ? "-0" : "-") + day +
                        (hour < 10 ? "T0" : "T") + hour;
            case DAYS:
                return year +
                        (month < 10 ? "-0" : "-") + month +
                        (day < 10 ? "-0" : "-") + day;
            case MONTHS:
                return year +
                        (month < 10 ? "-0" : "-") + month;
            case YEARS:
                return String.valueOf(year);
            default:
                return toIsoString(start);
        }
    }

    @MightBePromoted
    private static <T extends Comparable<T>> T min(T first, T second) {
        return first.compareTo(second) < 0 ? first : second;
    }

    private static ChronoUnit getMinChronoUnit(int second, int minute, int hour, int day, int month) {
        if (second != 0) return ChronoUnit.SECONDS;
        if (minute != 0) return ChronoUnit.MINUTES;
        if (hour != 0) return ChronoUnit.HOURS;
        if (day != 1) return ChronoUnit.DAYS;
        if (month != 1) return ChronoUnit.MONTHS;
        return ChronoUnit.YEARS;
    }

    private static final DateTimeFormatter AT_LEAST_YEAR = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .optionalStart().appendLiteral('-').appendPattern("MM")
            .optionalStart().appendLiteral('-').appendPattern("dd")
            .optionalStart().appendLiteral('T').appendPattern("HH")
            .optionalStart().appendLiteral(':').appendPattern("mm")
            .optionalStart().appendLiteral(':').appendPattern("ss")
            .optionalStart().appendFraction(NANO_OF_SECOND, 0, 9, true)
            .optionalEnd().optionalEnd().optionalEnd().optionalEnd().optionalEnd().optionalEnd()
            .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
            .toFormatter(Locale.ROOT);

    private static final char SOLIDUS = '/';

    private static String toIsoString(LocalDateTime start) {
        String result = start.toString();
        return result.length() == 16 ? (result + ":00") : result;
    }
}
