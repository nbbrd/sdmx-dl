package sdmxdl.format.time;

import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.Duration;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.format.DateTimeParseException;

/**
 * <pre>
 * 589 4.2.6 Standard Reporting Period
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
 */
@RepresentableAsString
@lombok.Value(staticConstructor = "of")
public class ReportingTimePeriod implements StandardTimePeriod {

    @StaticFactoryMethod
    public static @NonNull ReportingTimePeriod parse(@NonNull CharSequence text) throws DateTimeParseException {
        StandardReportingPeriod period = StandardReportingPeriod.parse(text);
        for (StandardReportingFormat format : StandardReportingFormat.VALUES) {
            if (period.isCompatibleWith(format)) {
                return new ReportingTimePeriod(format, period);
            }
        }
        throw new DateTimeParseException("Cannot parse", text, -1);
    }

    @StaticFactoryMethod
    public static @NonNull ReportingTimePeriod parseWith(@NonNull CharSequence text, @NonNull StandardReportingFormat format) throws DateTimeParseException {
        StandardReportingPeriod period = StandardReportingPeriod.parse(text);
        if (period.isCompatibleWith(format)) {
            return new ReportingTimePeriod(format, period);
        }
        throw new DateTimeParseException("Cannot parse", text, -1);
    }

    public static boolean isParsable(@Nullable CharSequence text) {
        return StandardReportingPeriod.isParsable(text);
    }

    public static boolean isParsableWith(@Nullable CharSequence text, @NonNull StandardReportingFormat format) {
        return StandardReportingPeriod.isParsable(text) && StandardReportingPeriod.isValidIndicator(text, format.getIndicator());
    }

    @NonNull StandardReportingFormat format;

    @NonNull StandardReportingPeriod period;

    @Override
    public String toString() {
        return period.toString();
    }

    @Override
    public @NonNull LocalDateTime toStartTime(@Nullable MonthDay reportingYearStartDay) {
        return period.toStartDate(format, reportingYearStartDay).atStartOfDay();
    }

    @Override
    public @NonNull Duration getDuration() {
        return format.getDuration();
    }
}
