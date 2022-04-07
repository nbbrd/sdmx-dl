package internal.sdmxdl.provider.ri.web.drivers;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.web.WebDriverAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class InseeDriver2Test {

    @Test
    public void testCompliance() {
        WebDriverAssert.assertCompliance(new InseeDriver2());
    }

    @Test
    public void testPeriodParser() {
        assertThat(InseeDriver2.EXTENDED_PARSER.parse("2013", null)).isEqualTo("2013-01-01T00:00:00");
        assertThat(InseeDriver2.EXTENDED_PARSER.parse("2014-Q3", null)).isEqualTo("2014-07-01T00:00:00");
        assertThat(InseeDriver2.EXTENDED_PARSER.parse("1990-09", null)).isEqualTo("1990-09-01T00:00:00");
        assertThat(InseeDriver2.EXTENDED_PARSER.parse("2012-S2", null)).isEqualTo("2012-07-01T00:00:00");
        assertThat(InseeDriver2.EXTENDED_PARSER.parse("2012-S2", null)).isEqualTo("2012-07-01T00:00:00");
        assertThat(InseeDriver2.EXTENDED_PARSER.parse("2012-B2", null)).isEqualTo("2012-03-01T00:00:00");
    }
}
