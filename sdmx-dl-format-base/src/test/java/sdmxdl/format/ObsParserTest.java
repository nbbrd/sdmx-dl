/*
 * Copyright 2015 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package sdmxdl.format;

import org.junit.jupiter.api.Test;
import sdmxdl.Key;
import sdmxdl.format.time.ReportingTimePeriod;
import sdmxdl.format.time.StandardReportingPeriod;
import tests.sdmxdl.format.ObsParserAssert;

import java.util.function.UnaryOperator;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.format.time.StandardReportingFormat.REPORTING_QUARTER;
import static sdmxdl.format.time.StandardReportingFormat.REPORTING_WEEK;

/**
 * @author Philippe Charles
 */
public class ObsParserTest {

    @Test
    public void testCompliance() {
        ObsParserAssert.assertCompliance(
                ObsParser.newDefault(),
                ObsParserAssert.Sample
                        .builder()
                        .validKey(Key.builder(asList("a", "b"))::getItem)
                        .validAttributes(UnaryOperator.identity())
                        .validPeriod("2001")
                        .invalidPeriod("abc")
                        .validValue("3.14")
                        .invalidValue("xyz")
                        .build()
        );
    }

    @Test
    public void testPeriod() {
        ObsParser x = ObsParser.newDefault();

        assertThat(x.period("2010-Q2").parsePeriod()).isEqualTo(ReportingTimePeriod.of(REPORTING_QUARTER, StandardReportingPeriod.parse("2010-Q2")));
        assertThat(x.period("2000-W53").parsePeriod()).isEqualTo(ReportingTimePeriod.of(REPORTING_WEEK, StandardReportingPeriod.parse("2000-W53")));
        assertThat(x.period("2011-W36").parsePeriod()).isEqualTo(ReportingTimePeriod.of(REPORTING_WEEK, StandardReportingPeriod.parse("2011-W36")));
    }
}
