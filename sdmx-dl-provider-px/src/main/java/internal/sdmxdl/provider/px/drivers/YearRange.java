package internal.sdmxdl.provider.px.drivers;

import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.VisibleForTesting;
import org.jspecify.annotations.Nullable;
import sdmxdl.format.time.ObservationalTimePeriod;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

@VisibleForTesting
@RepresentableAsString
@lombok.Value(staticConstructor = "of")
public class YearRange implements ObservationalTimePeriod {

    @StaticFactoryMethod
    public static @NonNull YearRange parse(@NonNull CharSequence text) throws DateTimeParseException {
        if (!isParsable(text)) throw new DateTimeParseException("Cannot parse year range", text, 0);
        Year start = Year.parse(text.subSequence(0, 4));
        Year end = Year.parse(text.subSequence(4 + 1, text.length()));
        if (start.isAfter(end)) throw new DateTimeParseException("Cannot parse year range", text, 0);
        return new YearRange(start, end);
    }

    public static boolean isParsable(@Nullable CharSequence text) {
        return text != null
                && text.length() == 9
                && text.charAt(4) == '-';
    }

    @NonNull
    Year includedStartYear;

    @NonNull
    Year includedEndYear;

    @Override
    public @NonNull LocalDateTime toStartTime(@Nullable MonthDay ignore) {
        return includedStartYear.atDay(1).atStartOfDay();
    }

    @Override
    public sdmxdl.@NonNull Duration getDuration() {
        return sdmxdl.Duration.P1Y.multipliedBy(includedEndYear.compareTo(includedStartYear) + 1);
    }

    @Override
    public String toString() {
        return includedStartYear.get(ChronoField.YEAR) + "-" + includedEndYear.get(ChronoField.YEAR);
    }
}
