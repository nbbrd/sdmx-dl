package sdmxdl.util.parser;

import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.List;
import java.util.Objects;

@FunctionalInterface
public interface TimeFormatParser {

    @Nullable LocalDateTime parse(@Nullable CharSequence text, @Nullable MonthDay reportingYearStartDay);

    default @NonNull TimeFormatParser orElse(@NonNull TimeFormatParser other) {
        Objects.requireNonNull(other);
        return (t, r) -> TimeFormatParsers.parse(t, r, this, other);
    }

    static @NonNull TimeFormatParser of(@NonNull Parser<LocalDateTime> parser) {
        Objects.requireNonNull(parser);
        return (t, r) -> parser.parse(t);
    }

    static @NonNull TimeFormatParser ofAll(@NonNull List<TimeFormatParser> parsers) {
        Objects.requireNonNull(parsers);
        return (t, r) -> TimeFormatParsers.parseAll(t, r, parsers);
    }

    static @NonNull TimeFormatParser onNull() {
        return (t, r) -> null;
    }

    static @NonNull TimeFormatParser onObservationalTimePeriod() {
        return TimeFormatParsers.OBSERVATIONAL;
    }

    static @NonNull TimeFormatParser onStandardReporting(@NonNull StandardReportingFormat format) {
        Objects.requireNonNull(format);
        return (t, r) -> TimeFormatParsers.parseStartTime(t, r, format);
    }
}
