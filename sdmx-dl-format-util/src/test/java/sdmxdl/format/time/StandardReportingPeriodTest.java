package sdmxdl.format.time;

import org.junit.jupiter.api.Test;
import sdmxdl.format.time.StandardReportingPeriod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class StandardReportingPeriodTest {

    final StandardReportingPeriod s = StandardReportingPeriod
            .builder()
            .reportingYear(2010)
            .periodIndicator('S')
            .periodValue(2)
            .periodValueDigits(1)
            .build();

    final StandardReportingPeriod m2 = StandardReportingPeriod
            .builder()
            .reportingYear(2010)
            .periodIndicator('M')
            .periodValue(3)
            .periodValueDigits(2)
            .build();

    final StandardReportingPeriod m1 = StandardReportingPeriod
            .builder()
            .reportingYear(2010)
            .periodIndicator('M')
            .periodValue(3)
            .periodValueDigits(1)
            .build();

    @Test
    public void testToString() {
        assertThat(s.toString()).isEqualTo("2010-S2");
        assertThat(m2.toString()).isEqualTo("2010-M03");
    }

    @Test
    public void testParse() {
        assertThat(StandardReportingPeriod.parse("2010-S2")).isEqualTo(s);
        assertThat(StandardReportingPeriod.parse("2010-M03")).isEqualTo(m2);
        assertThat(StandardReportingPeriod.parse("2010-M3")).isEqualTo(m1);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> StandardReportingPeriod.parse("2010-M"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> StandardReportingPeriod.parse("2010-03"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> StandardReportingPeriod.parse("2010M03"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> StandardReportingPeriod.parse("010-M03"));
    }
}
