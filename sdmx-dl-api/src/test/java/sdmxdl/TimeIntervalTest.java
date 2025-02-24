package sdmxdl;

import nbbrd.design.MightBePromoted;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.Duration.*;
import static sdmxdl.TimeInterval.parse;

public class TimeIntervalTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testFactories() {
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

        for (Duration d : new Duration[]{P1Y, P1M, P1D, PT1H, PT1M, PT1S}) {
            assertThat(parse("2010-02-15T11:22:33.444/" + d))
                    .returns(D2010_02_15T11_22_33_444, TimeInterval::getStart)
                    .returns(d, TimeInterval::getDuration);

            assertThat(parse("2010-02-15T11:22:33/" + d))
                    .returns(D2010_02_15T11_22_33, TimeInterval::getStart)
                    .returns(d, TimeInterval::getDuration);

            assertThat(parse("2010-02-15T11:22/" + d))
                    .returns(D2010_02_15T11_22, TimeInterval::getStart)
                    .returns(d, TimeInterval::getDuration);

            assertThat(parse("2010-02-15T11/" + d))
                    .returns(D2010_02_15T11_00, TimeInterval::getStart)
                    .returns(d, TimeInterval::getDuration);

            assertThat(parse("2010-02-15/" + d))
                    .returns(D2010_02_15T00_00, TimeInterval::getStart)
                    .returns(d, TimeInterval::getDuration);

            assertThat(parse("2010-02/" + d))
                    .returns(D2010_02_01T00_00, TimeInterval::getStart)
                    .returns(d, TimeInterval::getDuration);

            assertThat(parse("2010/" + d))
                    .returns(D2010_01_01T00_00, TimeInterval::getStart)
                    .returns(d, TimeInterval::getDuration);
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "StartDurationExamples.csv", useHeadersInDisplayName = true)
    public void testRepresentableAsString(String input, String output, String reduced) {
        if (output.equals("!")) {
            assertThatExceptionOfType(DateTimeParseException.class)
                    .isThrownBy(() -> parse(input));
        } else {
            assertThat(parse(input))
                    .hasToString(output)
                    .returns(reduced, TimeInterval::toShortString);
        }
    }

    private static final LocalDateTime D2010_02_15T11_22_33_444 = LocalDateTime.parse("2010-02-15T11:22:33.444");
    private static final LocalDateTime D2010_02_15T11_22_33 = LocalDateTime.parse("2010-02-15T11:22:33");
    private static final LocalDateTime D2010_02_15T11_22 = LocalDateTime.parse("2010-02-15T11:22");
    private static final LocalDateTime D2010_02_15T11_00 = LocalDateTime.parse("2010-02-15T11:00");
    private static final LocalDateTime D2010_02_15T00_00 = LocalDateTime.parse("2010-02-15T00:00");
    private static final LocalDateTime D2010_02_01T00_00 = LocalDateTime.parse("2010-02-01T00:00");
    private static final LocalDateTime D2010_01_01T00_00 = LocalDateTime.parse("2010-01-01T00:00");

    @MightBePromoted
    private static final Duration PT1H = Duration.parse("PT1H");
    @MightBePromoted
    private static final Duration PT1M = Duration.parse("PT1M");
    @MightBePromoted
    private static final Duration PT1S = Duration.parse("PT1S");
}
