package sdmxdl.format.time;

import nbbrd.io.text.Parser;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.Period;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.format.time.ObsTimeParser.*;

public class ObsTimeParserTest {

    private final LocalDateTime dateTime = LocalDateTime.parse("2001-01-01T00:00:00");
    private final MonthDay reportingYearStartDay = MonthDay.of(1, 2);

    @Test
    public void testOnParser() {
        assertThatNullPointerException().isThrownBy(() -> onParser(null));

        assertThat(onParser(Parser.onNull())).satisfies(x -> {
            assertThat(x.parseStartTime(null, null)).isNull();
            assertThat(x.parseStartTime("abc", null)).isNull();
            assertThat(x.parseStartTime("2001-01-01T00:00:00", null)).isNull();
            assertThat(x.parseStartTime("2001-01-01T00:00:00", reportingYearStartDay)).isNull();
        });

        assertThat(onParser(Parser.onConstant(dateTime))).satisfies(x -> {
            assertThat(x.parseStartTime(null, null)).isEqualTo(dateTime);
            assertThat(x.parseStartTime("abc", null)).isEqualTo(dateTime);
            assertThat(x.parseStartTime("2001-01-01T00:00:00", null)).isEqualTo(dateTime);
            assertThat(x.parseStartTime("2001-01-01T00:00:00", reportingYearStartDay)).isEqualTo(dateTime);
        });

        assertThat(onParser(Parser.onDateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE_TIME, LocalDateTime::from))).satisfies(x -> {
            assertThat(x.parseStartTime(null, null)).isNull();
            assertThat(x.parseStartTime("abc", null)).isNull();
            assertThat(x.parseStartTime("2001-01-01T00:00:00", null)).isEqualTo(dateTime);
            assertThat(x.parseStartTime("2001-01-01T00:00:00", reportingYearStartDay)).isEqualTo(dateTime);
        });
    }

    @Test
    public void testOnStandardReporting() {
        assertThatNullPointerException().isThrownBy(() -> onStandardReporting(null));

        assertThat(onStandardReporting(StandardReportingFormat.REPORTING_DAY)).satisfies(x -> {
            assertThat(x.parseStartTime(null, null)).isNull();
            assertThat(x.parseStartTime("abc", null)).isNull();
            assertThat(x.parseStartTime("2001-D001", null)).isEqualTo(dateTime);
            assertThat(x.parseStartTime("2001-D001", reportingYearStartDay)).isEqualTo(dateTime.plusDays(1));
        });
    }

    @Test
    public void testOnTimeRange() {
        assertThatNullPointerException().isThrownBy(() -> onTimeRange(null));

        assertThat(onTimeRange(o -> null)).satisfies(x -> {
            assertThat(x.parseStartTime(null, null)).isNull();
            assertThat(x.parseStartTime("abc", null)).isNull();
            assertThat(x.parseStartTime("2001-01-01T00:00:00/P1D", null)).isNull();
            assertThat(x.parseStartTime("2001-01-01T00:00:00/P1D", reportingYearStartDay)).isNull();
        });

        assertThat(onTimeRange(o -> TimeRange.DateTimeRange.of(dateTime, Period.parse("P1D")))).satisfies(x -> {
            assertThat(x.parseStartTime(null, null)).isEqualTo(dateTime);
            assertThat(x.parseStartTime("abc", null)).isEqualTo(dateTime);
            assertThat(x.parseStartTime("2001-01-01T00:00:00/P1D", null)).isEqualTo(dateTime);
            assertThat(x.parseStartTime("2001-01-01T00:00:00/P1D", reportingYearStartDay)).isEqualTo(dateTime);
        });

        assertThat(onTimeRange(TimeRange.DateTimeRange::parse)).satisfies(x -> {
            assertThat(x.parseStartTime(null, null)).isNull();
            assertThat(x.parseStartTime("abc", null)).isNull();
            assertThat(x.parseStartTime("2001-01-01T00:00:00/P1D", null)).isEqualTo(dateTime);
            assertThat(x.parseStartTime("2001-01-01T00:00:00/P1D", reportingYearStartDay)).isEqualTo(dateTime);
        });
    }
}
