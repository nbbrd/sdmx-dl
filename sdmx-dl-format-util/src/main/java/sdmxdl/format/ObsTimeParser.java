package sdmxdl.format;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.format.time.*;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@FunctionalInterface
public interface ObsTimeParser {

    @Nullable ObservationalTimePeriod parse(@Nullable CharSequence text);

    @Deprecated
    default @Nullable LocalDateTime parseStartTime(@Nullable CharSequence text, @Nullable MonthDay reportingYearStartDay) {
        ObservationalTimePeriod result = parse(text);
        return result != null ? result.toStartTime(reportingYearStartDay) : null;
    }


    default @NonNull ObsTimeParser orElse(@NonNull ObsTimeParser other) {
        return text -> {
            ObservationalTimePeriod result = parse(text);
            return result != null ? result : other.parse(text);
        };
    }

    Consumer<? super Throwable> IGNORE_ERROR = ignore -> {
    };

    Predicate<? super CharSequence> IGNORE_FILTER = ignore -> true;

    static @NonNull ObsTimeParser onStandardReportingFormat(
            @NonNull StandardReportingFormat format,
            @NonNull Consumer<? super Throwable> onError
    ) {
        return onObservationalTimePeriodParser(ReportingTimePeriod::isParsable, t -> ReportingTimePeriod.parseWith(t, format), onError);
    }

    static @NonNull ObsTimeParser onObservationalTimePeriodParser(
            @NonNull Predicate<? super CharSequence> filter,
            @NonNull Function<? super CharSequence, ? extends ObservationalTimePeriod> parser,
            @NonNull Consumer<? super Throwable> onError
    ) {
        return text -> {
            try {
                if (filter.test(text)) {
                    return parser.apply(text);
                }
            } catch (Throwable ex) {
                onError.accept(ex);
            }
            return null;
        };
    }

    @Deprecated
    static ObsTimeParser getObservationalTimePeriod() {
        return getObservationalTimePeriod(IGNORE_ERROR);
    }

    static ObsTimeParser getObservationalTimePeriod(Consumer<? super Throwable> onError) {
        return getStandardTimePeriod(onError).orElse(getTimeRange(onError));
    }

    static ObsTimeParser getStandardTimePeriod(Consumer<? super Throwable> onError) {
        return getBasicTimePeriod(onError).orElse(getReportingTimePeriod(onError));
    }

    static ObsTimeParser getBasicTimePeriod(Consumer<? super Throwable> onError) {
        return getGregorianTimePeriod(onError).orElse(getDateTime(onError));
    }

    static ObsTimeParser getGregorianTimePeriod(Consumer<? super Throwable> onError) {
        return onObservationalTimePeriodParser(GregorianTimePeriod.Year::isParsable, GregorianTimePeriod.Year::parse, onError)
                .orElse(onObservationalTimePeriodParser(GregorianTimePeriod.YearMonth::isParsable, GregorianTimePeriod.YearMonth::parse, onError))
                .orElse(onObservationalTimePeriodParser(GregorianTimePeriod.Day::isParsable, GregorianTimePeriod.Day::parse, onError));
    }

    static ObsTimeParser getDateTime(Consumer<? super Throwable> onError) {
        return onObservationalTimePeriodParser(DateTime::isParsable, DateTime::parse, onError);
    }

    static ObsTimeParser getReportingTimePeriod(Consumer<? super Throwable> onError) {
        return onObservationalTimePeriodParser(ReportingTimePeriod::isParsable, ReportingTimePeriod::parse, onError);
    }

    static ObsTimeParser getTimeRange(Consumer<? super Throwable> onError) {
        return onObservationalTimePeriodParser(TimeRange.DateRange::isParsable, TimeRange.DateRange::parse, onError)
                .orElse(onObservationalTimePeriodParser(TimeRange.DateTimeRange::isParsable, TimeRange.DateTimeRange::parse, onError));
    }
}
