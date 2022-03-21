package sdmxdl.util.parser;

import lombok.NonNull;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.List;

@FunctionalInterface
public interface TimeFormatParser {

    @Nullable LocalDateTime parse(@Nullable CharSequence text, @Nullable MonthDay reportingYearStartDay);

    default @NonNull TimeFormatParser orElse(@NonNull TimeFormatParser other) {
        return (t, r) -> TimeFormatParsers.parse(t, r, this, other);
    }

    static @NonNull TimeFormatParser of(@NonNull Parser<LocalDateTime> parser) {
        return (t, r) -> parser.parse(t);
    }

    static @NonNull TimeFormatParser ofAll(@NonNull List<TimeFormatParser> parsers) {
        return (t, r) -> TimeFormatParsers.parseAll(t, r, parsers);
    }

    static @NonNull TimeFormatParser onNull() {
        return (t, r) -> null;
    }

    static @NonNull TimeFormatParser onObservationalTimePeriod() {
        return TimeFormatParsers.OBSERVATIONAL;
    }

    static @NonNull TimeFormatParser onStandardReporting(@NonNull StandardReportingFormat format) {
        return (t, r) -> TimeFormatParsers.parseStartTime(t, r, format);
    }
}
