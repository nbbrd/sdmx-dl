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
import sdmxdl.format.ObsParser;
import tests.sdmxdl.format.ObsParserAssert;

import java.util.function.UnaryOperator;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

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

        UnaryOperator<String> none = o -> null;
        UnaryOperator<String> id1 = singletonMap("REPORTING_YEAR_START_DAY", "--07-01")::get;
        UnaryOperator<String> id2 = singletonMap("REPYEARSTART", "--07-01")::get;

        assertThat(x.period("2010-Q2").parsePeriod(none)).isEqualTo("2010-04-01T00:00:00");
        assertThat(x.period("2010-Q2").parsePeriod(id1)).isEqualTo("2010-10-01T00:00:00");
        assertThat(x.period("2010-Q2").parsePeriod(id2)).isEqualTo("2010-10-01T00:00:00");

        assertThat(x.period("2000-W53").parsePeriod(none)).isEqualTo("2001-01-01T00:00:00");
        assertThat(x.period("2011-W36").parsePeriod(none)).isEqualTo("2011-09-05T00:00:00");
        assertThat(x.period("2011-W36").parsePeriod(id1)).isEqualTo("2012-03-05T00:00:00");
        assertThat(x.period("2011-W36").parsePeriod(id2)).isEqualTo("2012-03-05T00:00:00");
    }
}
