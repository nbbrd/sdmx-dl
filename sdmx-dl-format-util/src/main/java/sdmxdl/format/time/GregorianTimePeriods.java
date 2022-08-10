package sdmxdl.format.time;

import lombok.NonNull;
import nbbrd.io.text.Parser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@lombok.experimental.UtilityClass
class GregorianTimePeriods {

    static final ObsTimeParser GREGORIAN_YEAR = ObsTimeParser.onParser(Parser.of(GregorianTimePeriods::parseStrictYear).andThen(GregorianTimePeriods::atStartOfYear));

    static final ObsTimeParser GREGORIAN_YEAR_MONTH = ObsTimeParser.onParser(Parser.of(YearMonth::parse).andThen(GregorianTimePeriods::atStartOfMonth));

    static final ObsTimeParser GREGORIAN_DAY = ObsTimeParser.onParser(Parser.of(LocalDate::parse).andThen(GregorianTimePeriods::atStartOfDay));

    // JDK > 8 changed parsing behavior of Year#parse(CharSequence) to accept min 1 digit instead of 4
    private static final DateTimeFormatter STRICT_YEAR_PARSER = DateTimeFormatter.ofPattern("uuuu");

    private static @NonNull Year parseStrictYear(@NonNull CharSequence input) {
        return Year.parse(input, STRICT_YEAR_PARSER);
    }

    private static LocalDateTime atStartOfYear(Year o) {
        return o != null ? LocalDateTime.of(o.getValue(), 1, 1, 0, 0) : null;
    }

    private static LocalDateTime atStartOfMonth(YearMonth o) {
        return o != null ? LocalDateTime.of(o.getYear(), o.getMonth(), 1, 0, 0) : null;
    }

    private static LocalDateTime atStartOfDay(LocalDate o) {
        return o != null ? o.atStartOfDay() : null;
    }
}
