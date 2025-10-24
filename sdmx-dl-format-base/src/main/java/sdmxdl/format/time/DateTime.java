package sdmxdl.format.time;

import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import org.jspecify.annotations.Nullable;
import sdmxdl.Duration;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.format.DateTimeParseException;

/**
 * <pre>
 * 583 4.2.5 Date Time
 * 584 This is used to unambiguously state that a date-time represents an observation at a
 * 585 single point in time. Therefore, if one wants to use SDMX for data which is measured
 * 586 at a distinct point in time rather than being reported over a period, the date-time
 * 587 representation can be used.
 * 588   Representation: xs:dateTime (YYYY-MM-DDThh:mm:ss)
 * </pre>
 * <pre>
 *     The seconds can be reported fractionally
 * </pre>
 */
@RepresentableAsString
@lombok.Value(staticConstructor = "of")
public class DateTime implements BasicTimePeriod {

    @StaticFactoryMethod
    public static @NonNull DateTime parse(@NonNull CharSequence text) throws DateTimeParseException {
        return new DateTime(java.time.LocalDateTime.parse(text));
    }

    public static boolean isParsable(@Nullable CharSequence text) {
        return text != null
                && text.length() >= 16
                && text.charAt(4) == '-'
                && text.charAt(7) == '-'
                && text.charAt(10) == 'T'
                && text.charAt(13) == ':'
                && TimeFormats.indexOf(text, '/') == -1;
    }

    @NonNull java.time.LocalDateTime dateTime;

    @Override
    public String toString() {
        return dateTime.toString();
    }

    @Override
    public @NonNull LocalDateTime toStartTime(@Nullable MonthDay reportingYearStartDay) {
        return dateTime;
    }

    @Override
    public @NonNull Duration getDuration() {
        return Duration.ZERO;
    }
}
