package sdmxdl.testing;

import lombok.AccessLevel;
import nbbrd.design.RepresentableAsString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.*;

@RepresentableAsString
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.Value
public class IntRange {

    int lowerBound;

    boolean lowerBoundOpen;

    int upperBound;

    boolean upperBoundOpen;

    public boolean contains(int value) {
        return (lowerBoundOpen ? lowerBound < value : lowerBound <= value)
                && (upperBoundOpen ? value < upperBound : value <= upperBound);
    }

    public boolean hasLowerBound() {
        return lowerBound != MIN_VALUE;
    }

    public boolean hasUpperBound() {
        return upperBound != MAX_VALUE;
    }

    @Override
    public String toString() {
        return (hasLowerBound() ? (lowerBoundOpen ? LOWER_BOUNDED_OPEN : LOWER_BOUNDED_CLOSED) + lowerBound : LOWER_UNBOUNDED)
                + SEPARATOR +
                (hasUpperBound() ? upperBound + (upperBoundOpen ? UPPER_BOUNDED_OPEN : UPPER_BOUNDED_CLOSED) : UPPER_UNBOUNDED);
    }

    public String toShortString() {
        if (upperBound == lowerBound && !lowerBoundOpen && !upperBoundOpen) {
            return String.valueOf(lowerBound);
        }
        return (hasLowerBound() ? (lowerBoundOpen ? LOWER_BOUNDED_OPEN : "") + lowerBound : "")
                + SEPARATOR +
                (hasUpperBound() ? upperBound + (upperBoundOpen ? UPPER_BOUNDED_OPEN : "") : "");
    }

    private static final Pattern PATTERN = Pattern.compile("([\\[(]?)(\\d+|(?:-\u221E)?)\\.\\.(\\d+|(?:\\+\u221E)?)([])]?)");

    public static IntRange parse(CharSequence text) throws IllegalArgumentException {
        Matcher matcher = PATTERN.matcher(text);

        if (!matcher.matches()) {
            try {
                return of(parseInt(text.toString()));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(text.toString());
            }
        }

        boolean lowerUnbounded = LOWER_UNBOUNDED.endsWith(matcher.group(2));
        boolean upperUnbounded = UPPER_UNBOUNDED.startsWith(matcher.group(3));

        return new IntRange(
                lowerUnbounded ? MIN_VALUE : parseInt(matcher.group(2)),
                lowerUnbounded || LOWER_BOUNDED_OPEN.equals(matcher.group(1)),
                upperUnbounded ? MAX_VALUE : parseInt(matcher.group(3)),
                upperUnbounded || UPPER_BOUNDED_OPEN.equals(matcher.group(4))
        );
    }

    private static final String LOWER_BOUNDED_CLOSED = "[";
    private static final String UPPER_BOUNDED_CLOSED = "]";
    private static final String LOWER_BOUNDED_OPEN = "(";
    private static final String UPPER_BOUNDED_OPEN = ")";
    private static final String LOWER_UNBOUNDED = "(-\u221E";
    private static final String UPPER_UNBOUNDED = "+\u221E)";
    private static final String SEPARATOR = "..";

    public static IntRange open(int lower, int upper) {
        return new IntRange(lower, true, upper, true);
    }

    public static IntRange closed(int lower, int upper) {
        return new IntRange(lower, false, upper, false);
    }

    public static IntRange openClosed(int lower, int upper) {
        return new IntRange(lower, true, upper, false);
    }

    public static IntRange closedOpen(int lower, int upper) {
        return new IntRange(lower, false, upper, true);
    }

    public static IntRange greaterThan(int lower) {
        return new IntRange(lower, true, MAX_VALUE, true);
    }

    public static IntRange atLeast(int lower) {
        return new IntRange(lower, false, MAX_VALUE, true);
    }

    public static IntRange lessThan(int upper) {
        return new IntRange(MIN_VALUE, true, upper, true);
    }

    public static IntRange atMost(int upper) {
        return new IntRange(MIN_VALUE, true, upper, false);
    }

    public static IntRange all() {
        return new IntRange(MIN_VALUE, true, MAX_VALUE, true);
    }

    public static IntRange of(int value) {
        return new IntRange(value, false, value, false);
    }
}
