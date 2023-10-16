package sdmxdl;

import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Simplified implementation of ISO-8601 time intervals that uses the <code>start/duration</code> expression.
 */
@RepresentableAsString
@lombok.Value(staticConstructor = "of")
public class TimeInterval {

    @StaticFactoryMethod
    public static @NonNull TimeInterval parse(@NonNull CharSequence text) throws DateTimeParseException {
        String textAsString = text.toString();
        int index = textAsString.indexOf(SEPARATOR);
        if (index == -1) {
            throw new DateTimeParseException("Cannot parse time interval", text, 0);
        }
        return new TimeInterval(
                LocalDateTime.parse(textAsString.substring(0, index), AT_LEAST_YEAR),
                Duration.parse(textAsString.substring(index + 1))
        );
    }

    @NonNull LocalDateTime start;

    @NonNull Duration duration;

    @Override
    public String toString() {
        return start.toString() + SEPARATOR + duration;
    }

    public @NonNull String toShortString() {
        return getStartAsShortString() + SEPARATOR + duration;
    }

    public @NonNull String getStartAsShortString() {
        ChronoUnit minChronoUnit = getMinChronoUnit(duration);
        int index = Math.max(getMinIndex(minChronoUnit), getDefaultingIndex(start));
        return start.toString().substring(0, index);
    }

    private static int getMinIndex(ChronoUnit unit) {
        switch (unit) {
            case SECONDS:
                return 19;
            case MINUTES:
                return 16;
            case HOURS:
                return 13;
            case DAYS:
                return 10;
            case MONTHS:
                return 7;
            case YEARS:
                return 4;
            default:
                return 23;
        }
    }

    private static int getDefaultingIndex(LocalDateTime start) {
        if (start.getNano() != 0)
            return 23;
        if (start.getSecond() != 0)
            return 19;
        if (start.getMinute() != 0)
            return 16;
        if (start.getHour() != 0)
            return 13;
        if (start.getDayOfMonth() != 1)
            return 10;
        if (start.getMonthValue() != 1)
            return 7;
        return 4;
    }

    private static ChronoUnit getMinChronoUnit(Duration duration) {
        return duration.getUnits().stream()
                .filter(o -> duration.get(o) != 0)
                .filter(ChronoUnit.class::isInstance)
                .map(ChronoUnit.class::cast)
                .sorted()
                .findFirst()
                .orElse(ChronoUnit.FOREVER);
    }

    private static final DateTimeFormatter AT_LEAST_YEAR = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendPattern("['-'MM['-'dd['T'HH[':'mm[':'ss['.'SSS]]]]]]")
            .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
            .toFormatter(Locale.ROOT);

    private static final char SEPARATOR = '/';
}
