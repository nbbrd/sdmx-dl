package sdmxdl.format.time;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDate;
import java.time.MonthDay;

/**
 * SDMX technical notes:
 * <pre>
 * 590 Standard reporting periods are periods of time in relation to a reporting year. Each of
 * 591 these standard reporting periods has a duration (based on the ISO 8601 definition)
 * 592 associated with it. The general format of a reporting period is as follows:
 * 593
 * 594   [REPORTING_YEAR]-[PERIOD_INDICATOR][PERIOD_VALUE]
 * 595
 * 596   Where:
 * 597     REPORTING_YEAR represents the reporting year as four digits (YYYY)
 * 598     PERIOD_INDICATOR identifies the type of period which determines the
 * 599     duration of the period
 * 600     PERIOD_VALUE indicates the actual period within the year
 * </pre>
 *
 * @see StandardReportingFormat
 */
@RepresentableAsString
@lombok.Value
@lombok.Builder
public class StandardReportingPeriod {

    private static final char SEPARATOR_CHAR = '-';
    private static final int SEPARATOR_INDEX = 4;
    private static final int PERIOD_INDICATOR_INDEX = 5;
    private static final int MIN_SIZE = 6;

    @NonNegative
    int reportingYear;

    char periodIndicator;

    @NonNegative
    int periodValue;

    @NonNegative
    int periodValueDigits;

    public @NonNull LocalDate toStartDate(@NonNull StandardReportingFormat format, @Nullable MonthDay reportingYearStartDay) {
        LocalDate reportingYearStartDate = (reportingYearStartDay != null ? reportingYearStartDay : FIRST_DAY_OF_YEAR).atYear(reportingYear);
        LocalDate reportingYearBase = format.getYearBaseFunction().apply(reportingYearStartDate);
        return reportingYearBase.plus(format.getAmounts().get(periodValue - 1));
    }

    public boolean isCompatibleWith(@NonNull StandardReportingFormat format) {
        return periodIndicator == format.getIndicator()
                && periodValueDigits == format.getPeriodValueDigits()
                && isPeriodValueInRange(periodValue, format.getLimitPerYear());
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        pad(result, reportingYear, 4);
        result.append(SEPARATOR_CHAR);
        result.append(periodIndicator);
        pad(result, periodValue, periodValueDigits);
        return result.toString();
    }

    @StaticFactoryMethod
    public static @NonNull StandardReportingPeriod parse(@NonNull CharSequence text) {
        StandardReportingPeriod result = parseOrNull(text);
        if (result == null) {
            throw new IllegalArgumentException(text.toString());
        }
        return result;
    }

    static @Nullable StandardReportingPeriod parseOrNull(@Nullable CharSequence text) {
        if (!isValidGeneralFormat(text)) {
            return null;
        }
        int reportingYear = parseNumeric(text, 0, 4);
        if (reportingYear == -1) {
            return null;
        }
        int periodValue = parseNumeric(text, MIN_SIZE, text.length());
        if (periodValue == -1) {
            return null;
        }
        return new StandardReportingPeriod(reportingYear, text.charAt(PERIOD_INDICATOR_INDEX), periodValue, text.length() - MIN_SIZE);
    }

    private static boolean isValidGeneralFormat(CharSequence input) {
        return input != null
                && input.length() > MIN_SIZE
                && input.charAt(SEPARATOR_INDEX) == SEPARATOR_CHAR
                && Character.isLetter(input.charAt(PERIOD_INDICATOR_INDEX))
                && Character.isUpperCase(input.charAt(PERIOD_INDICATOR_INDEX));
    }

    private static boolean isPeriodValueInRange(int periodValue, int limitPerYear) {
        return isInRange(periodValue, 1, limitPerYear + 1);
    }

    @MightBePromoted
    private static int parseNumeric(CharSequence text, int start, int end) {
        int result = 0;
        for (int i = start; i < end; i++) {
            int c = text.charAt(i) - '0';
            if (c < 0 || c > 9) {
                return -1;
            }
            result = result * 10 + c;
        }
        return result;
    }

    @MightBePromoted
    private static boolean isInRange(int value, int startInclusive, int endExclusive) {
        return startInclusive <= value && value < endExclusive;
    }

    private static void pad(StringBuilder result, int value, int digits) {
        int tmp = digits - StandardReportingFormat.getNumberOfDigits(value);
        for (int i = 0; i < tmp; i++) {
            result.append('0');
        }
        result.append(value);
    }

    private static final MonthDay FIRST_DAY_OF_YEAR = MonthDay.of(1, 1);
}
