package sdmxdl.util.parser;

import nbbrd.design.MightBePromoted;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;
import static sdmxdl.util.parser.StandardReportingFormat.*;

/**
 * Time formats as described in the SDMX21 technical notes:
 * https://sdmx.org/wp-content/uploads/SDMX_2-1_SECTION_6_TechnicalNotes_2020-07.pdf
 */
@lombok.experimental.UtilityClass
public class TimeFormatParsers {

    public static final MonthDay FIRST_DAY_OF_YEAR = MonthDay.of(1, 1);

    public static @NonNull Parser<LocalDateTime> getObservationalTimePeriod(@NonNull MonthDay reportingYearStartDay) {
        Objects.requireNonNull(reportingYearStartDay);
        return FIRST_DAY_OF_YEAR.equals(reportingYearStartDay) ? DEFAULT : newObservationalTimePeriod(reportingYearStartDay);
    }

    public static @NonNull Parser<LocalDateTime> getStartTimeParser(@NonNull StandardReportingFormat format, @NonNull MonthDay reportingYearStartDay) {
        Objects.requireNonNull(format);
        Objects.requireNonNull(reportingYearStartDay);
        return text -> parseStartTime(text, format, reportingYearStartDay);
    }

    private static LocalDateTime parseStartTime(CharSequence text, StandardReportingFormat format, MonthDay reportingYearStartDay) {
        StandardReportingPeriod period = StandardReportingPeriod.parseOrNull(text);
        return period != null && period.isValid(format)
                ? period.getStart(format, reportingYearStartDay).atStartOfDay()
                : null;
    }

    private static Parser<LocalDateTime> newObservationalTimePeriod(MonthDay reportingYearStartDay) {
        return allOf(
                asList(
                        GREGORIAN_YEAR,
                        GREGORIAN_YEAR_MONTH,
                        GREGORIAN_DAY,
                        DATE_TIME,
                        getStartTimeParser(YEAR, reportingYearStartDay),
                        getStartTimeParser(SEMESTER, reportingYearStartDay),
                        getStartTimeParser(TRIMESTER, reportingYearStartDay),
                        getStartTimeParser(QUARTER, reportingYearStartDay),
                        getStartTimeParser(MONTH, reportingYearStartDay),
                        getStartTimeParser(WEEK, reportingYearStartDay),
                        getStartTimeParser(DAY, reportingYearStartDay),
                        TIME_RANGE
                )
        );
    }

    // JDK > 8 changed parsing behavior of Year#parse(CharSequence) to accept min 1 digit instead of 4
    private static final DateTimeFormatter STRICT_YEAR_PARSER = DateTimeFormatter.ofPattern("uuuu");

    private static final Parser<LocalDateTime> GREGORIAN_YEAR = Parser.of(input -> Year.parse(input, STRICT_YEAR_PARSER)).andThen(TimeFormatParsers::atStartOfYear);
    private static final Parser<LocalDateTime> GREGORIAN_YEAR_MONTH = Parser.of(YearMonth::parse).andThen(TimeFormatParsers::atStartOfMonth);
    private static final Parser<LocalDateTime> GREGORIAN_DAY = Parser.of(LocalDate::parse).andThen(TimeFormatParsers::atStartOfDay);

    private static final Parser<LocalDateTime> DATE_TIME = Parser.of(LocalDateTime::parse);

    private static final Parser<LocalDateTime> TIME_RANGE = Parser.onNull(); // TODO

    private static final Parser<LocalDateTime> DEFAULT = newObservationalTimePeriod(FIRST_DAY_OF_YEAR);

    private static LocalDateTime atStartOfYear(Year o) {
        return o != null ? LocalDateTime.of(o.getValue(), 1, 1, 0, 0) : null;
    }

    private static LocalDateTime atStartOfMonth(YearMonth o) {
        return o != null ? LocalDateTime.of(o.getYear(), o.getMonth(), 1, 0, 0) : null;
    }

    private static LocalDateTime atStartOfDay(LocalDate o) {
        return o != null ? o.atStartOfDay() : null;
    }

    @MightBePromoted
    private static <T> Parser<T> allOf(List<Parser<? extends T>> parsers) {
        return input -> {
            for (Parser<? extends T> parser : parsers) {
                T result = parser.parse(input);
                if (result != null) {
                    return result;
                }
            }
            return null;
        };
    }
}
