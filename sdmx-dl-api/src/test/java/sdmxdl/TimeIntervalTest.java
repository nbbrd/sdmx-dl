package sdmxdl;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.TimeInterval.parse;

public class TimeIntervalTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testParse() {
        assertThatNullPointerException()
                .isThrownBy(() -> parse(null));

        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> parse("2010-02-15T00:00\\P2D"));

        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> parse("2010-02-15T00:00"));

        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> parse("P2D"));

        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> parse(""));

        assertThat(parse("2010-02-15T11:22/P2M"))
                .returns(LocalDateTime.parse("2010-02-15T11:22"), TimeInterval::getStart)
                .returns(P2M, TimeInterval::getDuration);

        assertThat(parse("2010-02-15T11/P2M"))
                .returns(LocalDateTime.parse("2010-02-15T11:00"), TimeInterval::getStart)
                .returns(P2M, TimeInterval::getDuration);

        assertThat(parse("2010-02-15/P2M"))
                .returns(LocalDateTime.parse("2010-02-15T00:00"), TimeInterval::getStart)
                .returns(P2M, TimeInterval::getDuration);

        assertThat(parse("2010-02/P2M"))
                .returns(LocalDateTime.parse("2010-02-01T00:00"), TimeInterval::getStart)
                .returns(P2M, TimeInterval::getDuration);

        assertThat(parse("2010/P2M"))
                .returns(LocalDateTime.parse("2010-01-01T00:00"), TimeInterval::getStart)
                .returns(P2M, TimeInterval::getDuration);
    }

    @Test
    public void testToShortString() {
        assertThat(parse("2010-02-15T11:22/P1Y").toShortString())
                .isEqualTo("2010-02-15T11:22/P1Y");
        assertThat(parse("2010-02-15T11/P1Y").toShortString())
                .isEqualTo("2010-02-15T11/P1Y");
        assertThat(parse("2010-02-15/P1Y").toShortString())
                .isEqualTo("2010-02-15/P1Y");
        assertThat(parse("2010-02/P1Y").toShortString())
                .isEqualTo("2010-02/P1Y");
        assertThat(parse("2010/P1Y").toShortString())
                .isEqualTo("2010/P1Y");

        assertThat(parse("2010-02-15T11:22/P2M").toShortString())
                .isEqualTo("2010-02-15T11:22/P2M");
        assertThat(parse("2010-02-15T11/P2M").toShortString())
                .isEqualTo("2010-02-15T11/P2M");
        assertThat(parse("2010-02-15/P2M").toShortString())
                .isEqualTo("2010-02-15/P2M");
        assertThat(parse("2010-02/P2M").toShortString())
                .isEqualTo("2010-02/P2M");
        assertThat(parse("2010/P2M").toShortString())
                .isEqualTo("2010-01/P2M");

        assertThat(parse("2010-02-15T11:22/P3D").toShortString())
                .isEqualTo("2010-02-15T11:22/P3D");
        assertThat(parse("2010-02-15T11/P3D").toShortString())
                .isEqualTo("2010-02-15T11/P3D");
        assertThat(parse("2010-02-15/P3D").toShortString())
                .isEqualTo("2010-02-15/P3D");
        assertThat(parse("2010-02/P3D").toShortString())
                .isEqualTo("2010-02-01/P3D");
        assertThat(parse("2010/P3D").toShortString())
                .isEqualTo("2010-01-01/P3D");
    }

    private static final Duration P1Y = Duration.parse("P1Y");
    private static final Duration P2M = Duration.parse("P2M");
    private static final Duration P3D = Duration.parse("P3D");
}
