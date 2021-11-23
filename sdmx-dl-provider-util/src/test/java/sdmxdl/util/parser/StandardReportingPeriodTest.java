package sdmxdl.util.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class StandardReportingPeriodTest {

    final StandardReportingPeriod s = new StandardReportingPeriod(2010, 'S', 2, 1);
    final StandardReportingPeriod m2 = new StandardReportingPeriod(2010, 'M', 3, 2);
    final StandardReportingPeriod m1 = new StandardReportingPeriod(2010, 'M', 3, 1);

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
