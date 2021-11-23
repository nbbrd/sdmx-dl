package sdmxdl.util.parser;

import nbbrd.io.text.Parser;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static sdmxdl.util.parser.StandardReportingFormat.*;
import static sdmxdl.util.parser.TimeFormatParser.ofAll;

/**
 * Time formats as described in the SDMX21 technical notes:
 * https://sdmx.org/wp-content/uploads/SDMX_2-1_SECTION_6_TechnicalNotes_2020-07.pdf
 */
@lombok.experimental.UtilityClass
class TimeFormatParsers {

    // JDK > 8 changed parsing behavior of Year#parse(CharSequence) to accept min 1 digit instead of 4
    private static final DateTimeFormatter STRICT_YEAR_PARSER = DateTimeFormatter.ofPattern("uuuu");

    private static LocalDateTime atStartOfYear(Year o) {
        return o != null ? LocalDateTime.of(o.getValue(), 1, 1, 0, 0) : null;
    }

    private static LocalDateTime atStartOfMonth(YearMonth o) {
        return o != null ? LocalDateTime.of(o.getYear(), o.getMonth(), 1, 0, 0) : null;
    }

    private static LocalDateTime atStartOfDay(LocalDate o) {
        return o != null ? o.atStartOfDay() : null;
    }

    private static final MonthDay FIRST_DAY_OF_YEAR = MonthDay.of(1, 1);

    static LocalDateTime parseStartTime(CharSequence text, MonthDay reportingYearStartDay, StandardReportingFormat format) {
        StandardReportingPeriod period = StandardReportingPeriod.parseOrNull(text);
        return period != null && period.isValid(format)
                ? period.getStart(format, reportingYearStartDay != null ? reportingYearStartDay : FIRST_DAY_OF_YEAR).atStartOfDay()
                : null;
    }

    static LocalDateTime parse(CharSequence text, MonthDay reportingYearStartDay, TimeFormatParser first, TimeFormatParser second) {
        LocalDateTime result = first.parse(text, reportingYearStartDay);
        return result != null ? result : second.parse(text, reportingYearStartDay);
    }

    static LocalDateTime parseAll(CharSequence text, MonthDay reportingYearStartDay, List<TimeFormatParser> parsers) {
        for (TimeFormatParser parser : parsers) {
            LocalDateTime result = parser.parse(text, reportingYearStartDay);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    static final TimeFormatParser GREGORIAN_YEAR = TimeFormatParser.of(Parser.of(input -> Year.parse(input, STRICT_YEAR_PARSER)).andThen(TimeFormatParsers::atStartOfYear));
    static final TimeFormatParser GREGORIAN_YEAR_MONTH = TimeFormatParser.of(Parser.of(YearMonth::parse).andThen(TimeFormatParsers::atStartOfMonth));
    static final TimeFormatParser GREGORIAN_DAY = TimeFormatParser.of(Parser.of(LocalDate::parse).andThen(TimeFormatParsers::atStartOfDay));

    static final TimeFormatParser DATE_TIME = TimeFormatParser.of(Parser.of(LocalDateTime::parse));

    static final TimeFormatParser TIME_RANGE = TimeFormatParser.of(Parser.onNull()); // TODO

    static final TimeFormatParser GREGORIAN = GREGORIAN_YEAR.orElse(GREGORIAN_YEAR_MONTH).orElse(GREGORIAN_DAY);

    static final TimeFormatParser BASIC = GREGORIAN.orElse(DATE_TIME);

    static final TimeFormatParser REPORTING = ofAll(
            Stream.of(YEAR, SEMESTER, TRIMESTER, QUARTER, MONTH, WEEK, DAY)
                    .map(TimeFormatParser::onStandardReporting)
                    .collect(Collectors.toList())
    );

    static final TimeFormatParser STANDARD = BASIC.orElse(REPORTING);

    static final TimeFormatParser OBSERVATIONAL = STANDARD.orElse(TIME_RANGE);
}
