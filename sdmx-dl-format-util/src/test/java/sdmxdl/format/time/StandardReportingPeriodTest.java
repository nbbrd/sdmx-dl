package sdmxdl.format.time;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class StandardReportingPeriodTest {

    static final StandardReportingPeriod S = StandardReportingPeriod
            .builder()
            .reportingYear(2010)
            .periodIndicator('S')
            .periodValue(2)
            .periodValueDigits(1)
            .build();

    static final StandardReportingPeriod M2 = StandardReportingPeriod
            .builder()
            .reportingYear(2010)
            .periodIndicator('M')
            .periodValue(3)
            .periodValueDigits(2)
            .build();

    static final StandardReportingPeriod M1 = StandardReportingPeriod
            .builder()
            .reportingYear(2010)
            .periodIndicator('M')
            .periodValue(3)
            .periodValueDigits(1)
            .build();

    @Test
    public void testToString() {
        assertThat(S.toString()).isEqualTo("2010-S2");
        assertThat(M2.toString()).isEqualTo("2010-M03");
    }

    @Test
    public void testParse() {
        assertThat(StandardReportingPeriod.parse("2010-S2")).isEqualTo(S);
        assertThat(StandardReportingPeriod.parse("2010-M03")).isEqualTo(M2);
        assertThat(StandardReportingPeriod.parse("2010-M3")).isEqualTo(M1);

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
