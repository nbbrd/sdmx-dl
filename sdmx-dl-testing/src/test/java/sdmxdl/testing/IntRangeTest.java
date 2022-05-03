package sdmxdl.testing;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class IntRangeTest {

    @Test
    public void test() {
        assertThatNullPointerException()
                .isThrownBy(() -> IntRange.parse(null));

        assertThat(IntRange.open(2, 4))
                .isEqualTo(IntRange.parse("(2..4)"))
                .isEqualTo(IntRange.parse("(2..4)"))
                .hasToString("(2..4)")
                .has(toShortString("(2..4)"))
                .isNot(containing(1))
                .isNot(containing(2))
                .is(containing(3))
                .isNot(containing(4))
                .isNot(containing(5))
        ;

        assertThat(IntRange.closed(2, 4))
                .isEqualTo(IntRange.parse("[2..4]"))
                .isEqualTo(IntRange.parse("2..4"))
                .hasToString("[2..4]")
                .has(toShortString("2..4"))
                .isNot(containing(1))
                .is(containing(2))
                .is(containing(3))
                .is(containing(4))
                .isNot(containing(5))
        ;

        assertThat(IntRange.openClosed(2, 4))
                .isEqualTo(IntRange.parse("(2..4]"))
                .isEqualTo(IntRange.parse("(2..4"))
                .hasToString("(2..4]")
                .has(toShortString("(2..4"))
                .isNot(containing(1))
                .isNot(containing(2))
                .is(containing(3))
                .is(containing(4))
                .isNot(containing(5))
        ;

        assertThat(IntRange.closedOpen(2, 4))
                .isEqualTo(IntRange.parse("[2..4)"))
                .isEqualTo(IntRange.parse("2..4)"))
                .hasToString("[2..4)")
                .has(toShortString("2..4)"))
                .isNot(containing(1))
                .is(containing(2))
                .is(containing(3))
                .isNot(containing(4))
                .isNot(containing(5))
        ;

        assertThat(IntRange.greaterThan(2))
                .isEqualTo(IntRange.parse("(2..+∞)"))
                .isEqualTo(IntRange.parse("(2.."))
                .hasToString("(2..+∞)")
                .has(toShortString("(2.."))
                .isNot(containing(1))
                .isNot(containing(2))
                .is(containing(3))
                .is(containing(4))
                .is(containing(5))
        ;

        assertThat(IntRange.atLeast(2))
                .isEqualTo(IntRange.parse("[2..+∞)"))
                .isEqualTo(IntRange.parse("2..)"))
                .hasToString("[2..+∞)")
                .has(toShortString("2.."))
                .isNot(containing(1))
                .is(containing(2))
                .is(containing(3))
                .is(containing(4))
                .is(containing(5))
        ;

        assertThat(IntRange.lessThan(4))
                .isEqualTo(IntRange.parse("(-∞..4)"))
                .isEqualTo(IntRange.parse("..4)"))
                .hasToString("(-∞..4)")
                .has(toShortString("..4)"))
                .is(containing(1))
                .is(containing(2))
                .is(containing(3))
                .isNot(containing(4))
                .isNot(containing(5))
        ;

        assertThat(IntRange.atMost(4))
                .isEqualTo(IntRange.parse("(-∞..4]"))
                .isEqualTo(IntRange.parse("..4"))
                .hasToString("(-∞..4]")
                .has(toShortString("..4"))
                .is(containing(1))
                .is(containing(2))
                .is(containing(3))
                .is(containing(4))
                .isNot(containing(5))
        ;

        assertThat(IntRange.all())
                .isEqualTo(IntRange.parse("(-∞..+∞)"))
                .isEqualTo(IntRange.parse(".."))
                .hasToString("(-∞..+∞)")
                .has(toShortString(".."))
                .is(containing(1))
                .is(containing(2))
                .is(containing(3))
                .is(containing(4))
                .is(containing(5))
        ;

        assertThat(IntRange.of(3))
                .isEqualTo(IntRange.parse("[3..3]"))
                .isEqualTo(IntRange.parse("3"))
                .hasToString("[3..3]")
                .has(toShortString("3"))
                .isNot(containing(1))
                .isNot(containing(2))
                .is(containing(3))
                .isNot(containing(4))
                .isNot(containing(5))
        ;
    }

    Condition<IntRange> containing(int value) {
        return new Condition<>(range -> range.contains(value), "containing %s", value);
    }

    Condition<IntRange> toShortString(String text) {
        return new Condition<>(range -> range.toShortString().equals(text), "short string equals to %s", text);
    }
}
