package sdmxdl;

import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Simplified implementation of ISO-8601 time intervals that uses the <code>start/duration</code> expression.
 */
@RepresentableAsString
@lombok.Value(staticConstructor = "of")
public class TimeInterval {

    @StaticFactoryMethod
    public static @NonNull TimeInterval parse(@NonNull CharSequence text) throws DateTimeParseException {
        String textAsString = text.toString();
        int index = textAsString.indexOf('/');
        if (index == -1) {
            throw new DateTimeParseException("Cannot parse time interval", text, 0);
        }
        return new TimeInterval(
                LocalDateTime.parse(textAsString.substring(0, index)),
                Duration.parse(textAsString.substring(index + 1))
        );
    }

    @NonNull LocalDateTime start;

    @NonNull Duration duration;

    @Override
    public String toString() {
        return start + "/" + duration;
    }
}
