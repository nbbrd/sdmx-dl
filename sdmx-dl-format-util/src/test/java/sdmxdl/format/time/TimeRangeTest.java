package sdmxdl.format.time;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableTypeAssert;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class TimeRangeTest {

    private final Period P2D = Period.ofDays(2);
    private final Period P2M = Period.ofMonths(2);
    private final LocalDate startDate = LocalDate.of(2010, 2, 15);

    @Test
    public void testDate() {
        TimeRange.DateRange days = TimeRange.DateRange.of(startDate, P2D);
        TimeRange.DateRange months = TimeRange.DateRange.of(startDate, P2M);

        assertThat(TimeRange.DateRange.parse("2010-02-15/P2D")).isEqualTo(days);
        assertThat(TimeRange.DateRange.parse("2010-02-15/P2M")).isEqualTo(months);
        assertThatParseException().isThrownBy(() -> TimeRange.DateRange.parse("2010-02-15T00:00/P2D"));
        assertThatParseException().isThrownBy(() -> TimeRange.DateRange.parse("20100215/P2D"));
        assertThatParseException().isThrownBy(() -> TimeRange.DateRange.parse("2010-046/P2D"));
        assertThatParseException().isThrownBy(() -> TimeRange.DateRange.parse("2010-W07-1/P2D"));

        assertThatNullPointerException().isThrownBy(() -> TimeRange.DateRange.parse(null));
    }

    @Test
    public void testDateTime() {
        TimeRange.DateTimeRange days = TimeRange.DateTimeRange.of(startDate.atStartOfDay(), P2D);
        TimeRange.DateTimeRange months = TimeRange.DateTimeRange.of(startDate.atStartOfDay(), P2M);

        assertThat(TimeRange.DateTimeRange.parse("2010-02-15T00:00/P2D")).isEqualTo(days);
        assertThat(TimeRange.DateTimeRange.parse("2010-02-15T00:00/P2M")).isEqualTo(months);
        assertThatParseException().isThrownBy(() -> TimeRange.DateTimeRange.parse("2010-02-15/P2D"));
        assertThatParseException().isThrownBy(() -> TimeRange.DateTimeRange.parse("20100215/P2D"));
        assertThatParseException().isThrownBy(() -> TimeRange.DateTimeRange.parse("2010-046/P2D"));
        assertThatParseException().isThrownBy(() -> TimeRange.DateTimeRange.parse("2010-W07-1/P2D"));

        assertThatNullPointerException().isThrownBy(() -> TimeRange.DateTimeRange.parse(null));
    }

    private static ThrowableTypeAssert<DateTimeParseException> assertThatParseException() {
        return Assertions.assertThatExceptionOfType(DateTimeParseException.class);
    }
}
