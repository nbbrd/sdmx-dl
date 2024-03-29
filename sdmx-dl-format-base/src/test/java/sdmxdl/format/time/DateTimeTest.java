package sdmxdl.format.time;

import org.junit.jupiter.api.Test;
import sdmxdl.Duration;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.format.time.TimeFormatsTest.*;

public class DateTimeTest {

    @Test
    public void testOf() {
        assertThatNullPointerException()
                .isThrownBy(() -> DateTime.of(null));

        assertThat(DateTime.of(LDT_WITH_SECONDS))
                .isEqualTo(DateTime.of(LDT_WITH_SECONDS))
                .extracting(DateTime::getDateTime)
                .isEqualTo(LDT_WITH_SECONDS);
    }

    @Test
    public void testParse() {
        assertThatNullPointerException()
                .isThrownBy(() -> DateTime.parse(null));

        assertThat(DateTime.parse("2001-02-03T04:05:06"))
                .isEqualTo(DateTime.of(LDT_WITH_SECONDS));

        assertThat(generateInvalids("2001-02-03T04:05:06"))
                .describedAs("Invalid format")
                .are(throwingDateTimeParseExceptionOn(DateTime::parse));

        assertThat("2019-02-29T11:23:56")
                .describedAs("Out-of-bounds")
                .is(throwingDateTimeParseExceptionOn(DateTime::parse));

        assertThat(DateTime.parse("2001-02-03T04:05"))
                .isEqualTo(DateTime.of(LDT_WITHOUT_SECONDS));

        assertThat(generateInvalids("2001-02-03T04:05"))
                .describedAs("Invalid format")
                .are(throwingDateTimeParseExceptionOn(DateTime::parse));

        assertThat("2019-02-29T11:23")
                .describedAs("Out-of-bounds")
                .is(throwingDateTimeParseExceptionOn(DateTime::parse));
    }

    @Test
    public void testIsParsable() {
        assertThat(DateTime.isParsable(null)).isFalse();

        assertThat(DateTime.isParsable("2001-02-03T04:05:06")).isTrue();

        assertThat(generateInvalids("2001-02-03T04:05:06"))
                .filteredOn(not(DateTime::isParsable))
                .are(throwingDateTimeParseExceptionOn(DateTime::parse));

        assertThat(DateTime.isParsable("2001-02-03T04:05")).isTrue();

        assertThat(generateInvalids("2001-02-03T04:05"))
                .filteredOn(not(DateTime::isParsable))
                .are(throwingDateTimeParseExceptionOn(DateTime::parse));
    }

    @Test
    public void testToString() {
        assertThat(DateTime.of(LDT_WITH_SECONDS))
                .hasToString("2001-02-03T04:05:06");

        assertThat(DateTime.of(LDT_WITHOUT_SECONDS))
                .hasToString("2001-02-03T04:05");
    }

    @Test
    public void testToStartTime() {
        assertThat(DateTime.of(LDT_WITH_SECONDS).toStartTime(null))
                .isEqualTo(LDT_WITH_SECONDS);

        assertThat(DateTime.of(LDT_WITHOUT_SECONDS).toStartTime(null))
                .isEqualTo(LDT_WITHOUT_SECONDS);
    }

    @Test
    public void testGetDuration() {
        assertThat(DateTime.of(LDT_WITH_SECONDS).getDuration())
                .isEqualTo(Duration.ZERO);

        assertThat(DateTime.of(LDT_WITHOUT_SECONDS).getDuration())
                .isEqualTo(Duration.ZERO);
    }

    private static final LocalDateTime LDT_WITH_SECONDS = LocalDateTime.parse("2001-02-03T04:05:06");

    private static final LocalDateTime LDT_WITHOUT_SECONDS = LocalDateTime.parse("2001-02-03T04:05");
}
