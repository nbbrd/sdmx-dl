package sdmxdl;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

import java.time.DateTimeException;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeParseException;
import java.time.temporal.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.*;
import static java.time.temporal.ChronoUnit.*;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * An amount of time in the ISO-8601 calendar system, such as '2 years, 3 months and 4 days'.
 */
@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Duration implements ChronoPeriod {

    /**
     * A constant for a duration of zero.
     */
    public static final Duration ZERO = new Duration(0, 0, 0, 0, 0, 0, 0);

    private static final Pattern PATTERN = Pattern.compile("^P(?=\\d+[YMWD])(\\d+Y)?(\\d+M)?(\\d+W)?(\\d+D)?(T(?=\\d+[HMS])(\\d+H)?(\\d+M)?(\\d+S)?)?$");

    private static final List<TemporalUnit> SUPPORTED_UNITS = unmodifiableList(asList(YEARS, MONTHS, DAYS, HOURS, MINUTES, SECONDS, WEEKS));

    @StaticFactoryMethod
    public static @NonNull Duration parse(@NonNull CharSequence text) throws DateTimeParseException {
        Matcher m = PATTERN.matcher(text);
        if (!m.matches()) {
            throw new DateTimeParseException("Cannot parse using duration pattern", text, 0);
        }
        return m.group(3) == null
                ? create(toInt(m, 1), toInt(m, 2), toInt(m, 4), toInt(m, 6), toInt(m, 7), toInt(m, 8))
                : create(toInt(m, 3));
    }

    @StaticFactoryMethod
    public static @NonNull Duration from(@NonNull TemporalAmount amount) throws DateTimeException {
        if (amount instanceof Duration) {
            return (Duration) amount;
        }
        if (!hasIsoChronology(amount)) {
            throw new DateTimeException("Period requires ISO chronology: " + amount);
        }
        int years = 0;
        int months = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        for (TemporalUnit unit : amount.getUnits()) {
            long unitAmount = amount.get(unit);
            if (unit == YEARS) years = toIntExact(unitAmount);
            else if (unit == MONTHS) months = toIntExact(unitAmount);
            else if (unit == DAYS) days = toIntExact(unitAmount);
            else if (unit == HOURS) hours = toIntExact(unitAmount);
            else if (unit == MINUTES) minutes = toIntExact(unitAmount);
            else if (unit == SECONDS) seconds = toIntExact(unitAmount);
            else throw new DateTimeException("Unit must be Years, Months or Days, but was " + unit);
        }
        return create(years, months, days, hours, minutes, seconds);
    }

    private static Duration create(int years, int months, int days, int hours, int minutes, int seconds) {
        return (years | months | days | hours | minutes | seconds) == 0
                ? ZERO
                : new Duration(years, months, days, hours, minutes, seconds, 0);
    }

    private static Duration create(int weeks) {
        return weeks == 0 ? ZERO : new Duration(0, 0, 0, 0, 0, 0, weeks);
    }

    /**
     * Number of years.
     */
    int years;

    /**
     * Number of months.
     */
    int months;

    /**
     * Number of days.
     */
    int days;

    /**
     * Number of hours.
     */
    int hours;

    /**
     * Number of minutes.
     */
    int minutes;

    /**
     * Number of seconds.
     */
    int seconds;

    /**
     * Number of weeks.
     */
    int weeks;

    @Override
    public String toString() {
        if (weeks != 0) {
            return "P" + weeks + "W";
        } else {
            String result = "P";

            if (years != 0) result += years + "Y";
            if (months != 0) result += months + "M";
            if (days != 0) result += days + "D";

            if (hours != 0 || minutes != 0 || seconds != 0) {
                result += "T";
                if (hours != 0) result += hours + "H";
                if (minutes != 0) result += minutes + "M";
                if (seconds != 0) result += seconds + "S";
            } else if (result.length() == 1) {
                result += "0D";
            }

            return result;
        }
    }

    @Override
    public long get(@NonNull TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            switch ((ChronoUnit) unit) {
                case YEARS:
                    return years;
                case MONTHS:
                    return months;
                case DAYS:
                    return days;
                case HOURS:
                    return hours;
                case MINUTES:
                    return minutes;
                case SECONDS:
                    return seconds;
                case WEEKS:
                    return weeks;
            }
        }
        throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return SUPPORTED_UNITS;
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        Temporal result = temporal;
        if (years != 0) result = result.plus(years, YEARS);
        if (months != 0) result = result.plus(months, MONTHS);
        if (days != 0) result = result.plus(days, DAYS);
        if (hours != 0) result = result.plus(hours, HOURS);
        if (minutes != 0) result = result.plus(minutes, MINUTES);
        if (seconds != 0) result = result.plus(seconds, SECONDS);
        if (weeks != 0) result = result.plus(weeks, WEEKS);
        return result;
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        Temporal result = temporal;
        if (years != 0) result = result.minus(years, YEARS);
        if (months != 0) result = result.minus(months, MONTHS);
        if (days != 0) result = result.minus(days, DAYS);
        if (hours != 0) result = result.minus(hours, HOURS);
        if (minutes != 0) result = result.minus(minutes, MINUTES);
        if (seconds != 0) result = result.minus(seconds, SECONDS);
        if (weeks != 0) result = result.minus(weeks, WEEKS);
        return result;
    }

    @Override
    public Chronology getChronology() {
        return IsoChronology.INSTANCE;
    }

    @Override
    public Duration plus(@NonNull TemporalAmount amountToAdd) {
        Duration isoAmount = Duration.from(amountToAdd);
        return weeks == 0
                ? create(
                addExact(years, isoAmount.years),
                addExact(months, isoAmount.months),
                addExact(days, isoAmount.days),
                addExact(hours, isoAmount.hours),
                addExact(minutes, isoAmount.minutes),
                addExact(seconds, isoAmount.seconds))
                : create(addExact(weeks, isoAmount.weeks));
    }

    @Override
    public Duration minus(@NonNull TemporalAmount amountToSubtract) {
        Duration isoAmount = Duration.from(amountToSubtract);
        return weeks == 0
                ? create(
                subtractExact(years, isoAmount.years),
                subtractExact(months, isoAmount.months),
                subtractExact(days, isoAmount.days),
                subtractExact(hours, isoAmount.hours),
                subtractExact(minutes, isoAmount.minutes),
                subtractExact(seconds, isoAmount.seconds))
                : create(subtractExact(weeks, isoAmount.weeks));
    }

    public @NonNull Duration multipliedBy(int scalar) {
        if (this == ZERO || scalar == 1) {
            return this;
        }
        return weeks == 0
                ? create(
                multiplyExact(years, scalar),
                multiplyExact(months, scalar),
                multiplyExact(days, scalar),
                multiplyExact(hours, scalar),
                multiplyExact(minutes, scalar),
                multiplyExact(seconds, scalar))
                : create(multiplyExact(weeks, scalar));
    }

    @Override
    public Duration normalized() {
        return weeks == 0
                ? create(years + (months / 12), months % 12, days, hours, minutes, seconds)
                : this;
    }

    private static int toInt(Matcher matcher, int group) {
        String result = matcher.group(group);
        return result != null ? Integer.parseInt(result.substring(0, result.length() - 1)) : 0;
    }

    private static boolean hasIsoChronology(TemporalAmount amount) {
        return amount instanceof ChronoPeriod && IsoChronology.INSTANCE.equals(((ChronoPeriod) amount).getChronology());
    }
}
