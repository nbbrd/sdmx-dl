package sdmxdl.format.time;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.io.function.IOUnaryOperator;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.time.MonthDay;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Time formats as described in the <a href="https://sdmx.org/wp-content/uploads/SDMX_2-1_SECTION_6_TechnicalNotes_2020-07.pdf">SDMX21 technical notes</a>
 *
 * <pre>
 * 536 4.2 Time and Time Format
 * 537 4.2.1 Introduction
 * 538 First, it is important to recognize that most observation times are a period. SDMX
 * 539 specifies precisely how Time is handled.
 * 540
 * 541 The representation of time is broken into a hierarchical collection of representations.
 * 542 A data structure definition can use of any of the representations in the hierarchy as
 * 543 the representation of time. This allows for the time dimension of a particular data
 * 544 structure definition allow for only a subset of the default representation.
 * 545
 * 546 The hierarchy of time formats is as follows (bold indicates a category which is made
 * 547 up of multiple formats, italic indicates a distinct format):
 * 548
 * 549    Observational Time Period
 * 550     o Standard Time Period
 * 551        Basic Time Period
 * 552          Gregorian Time Period
 * 553          Date Time
 * 554        Reporting Time Period
 * 555     o Time Range
 * 556
 * 557 The details of these time period categories and of the distinct formats which make
 * 558 them up are detailed in the sections to follow.
 * </pre>
 */
public final class TimeFormats {

    private TimeFormats() {
    }

    public static final Consumer<? super Throwable> IGNORE_ERROR = ignore -> {
    };

    public static final Predicate<? super CharSequence> IGNORE_FILTER = ignore -> true;

    @MightBePromoted
    static int indexOf(CharSequence text, char c) {
        if (text instanceof String) {
            return ((String) text).indexOf(c);
        }
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    @MightBePromoted
    static int parseNumeric(CharSequence text, int start, int end) {
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
    static boolean isInRange(int value, int startInclusive, int endExclusive) {
        return startInclusive <= value && value < endExclusive;
    }

    @MightBePromoted
    static <T> @NonNull Parser<T> parserOf(
            @NonNull Predicate<? super CharSequence> filter,
            @NonNull Function<? super CharSequence, ? extends T> parser,
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

    // https://sis-cc.gitlab.io/dotstatsuite-documentation/using-api/typical-use-cases/#non-calendar-reporting-periods
    public static @Nullable MonthDay getReportingYearStartDay(@NonNull IOUnaryOperator<String> obsAttributes) throws IOException {
        String reportingYearStartDay = obsAttributes.applyWithIO("REPORTING_YEAR_START_DAY");
        if (reportingYearStartDay == null) {
            reportingYearStartDay = obsAttributes.applyWithIO("REPYEARSTART");
        }
        if (reportingYearStartDay == null) {
            return null;
        }
        return MONTH_DAY_PARSER.parse(reportingYearStartDay);
    }

    private static final Parser<MonthDay> MONTH_DAY_PARSER = Parser.of(MonthDay::parse);

    public static @NonNull Parser<ObservationalTimePeriod> onReportingFormat(
            @NonNull StandardReportingFormat format,
            @NonNull Consumer<? super Throwable> onError
    ) {
        return onParser(t -> ReportingTimePeriod.isParsableWith(t, format), t -> ReportingTimePeriod.parseWith(t, format), onError);
    }

    public static @NonNull Parser<ObservationalTimePeriod> onParser(
            @NonNull Predicate<? super CharSequence> filter,
            @NonNull Function<? super CharSequence, ? extends ObservationalTimePeriod> parser,
            @NonNull Consumer<? super Throwable> onError
    ) {
        return parserOf(filter, parser, onError);
    }

    public static Parser<ObservationalTimePeriod> getObservationalTimePeriod(Consumer<? super Throwable> onError) {
        return getStandardTimePeriod(onError).orElse(getTimeRange(onError));
    }

    public static Parser<ObservationalTimePeriod> getStandardTimePeriod(Consumer<? super Throwable> onError) {
        return getBasicTimePeriod(onError).orElse(getReportingTimePeriod(onError));
    }

    public static Parser<ObservationalTimePeriod> getBasicTimePeriod(Consumer<? super Throwable> onError) {
        return getGregorianTimePeriod(onError).orElse(getDateTime(onError));
    }

    public static Parser<ObservationalTimePeriod> getGregorianTimePeriod(Consumer<? super Throwable> onError) {
        return onParser(GregorianTimePeriod.Year::isParsable, GregorianTimePeriod.Year::parse, onError)
                .orElse(onParser(GregorianTimePeriod.YearMonth::isParsable, GregorianTimePeriod.YearMonth::parse, onError))
                .orElse(onParser(GregorianTimePeriod.Day::isParsable, GregorianTimePeriod.Day::parse, onError));
    }

    public static Parser<ObservationalTimePeriod> getDateTime(Consumer<? super Throwable> onError) {
        return onParser(DateTime::isParsable, DateTime::parse, onError);
    }

    public static Parser<ObservationalTimePeriod> getReportingTimePeriod(Consumer<? super Throwable> onError) {
        return onParser(ReportingTimePeriod::isParsable, ReportingTimePeriod::parse, onError);
    }

    public static Parser<ObservationalTimePeriod> getTimeRange(Consumer<? super Throwable> onError) {
        return onParser(TimeRange.DateRange::isParsable, TimeRange.DateRange::parse, onError)
                .orElse(onParser(TimeRange.DateTimeRange::isParsable, TimeRange.DateTimeRange::parse, onError));
    }
}
