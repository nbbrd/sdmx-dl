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
package sdmxdl.util.parser;

import nbbrd.io.text.Parser;
import org.junit.Test;
import sdmxdl.Frequency;
import sdmxdl.Key;
import sdmxdl.ext.ObsParser;
import sdmxdl.tck.ext.ObsParserAssert;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class DefaultObsParserTest {

    @Test
    public void testCompliance() {
        ObsParserAssert.assertCompliance(
                new DefaultObsParser((key, attributes) -> Frequency.ANNUAL, PeriodParsers::onStandardFreq, Parser.onDouble()),
                ObsParserAssert.Sample
                        .builder()
                        .validKey(Key.builder(asList("a", "b")))
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
        AtomicReference<Frequency> freq = new AtomicReference<>();
        ObsParser p = new DefaultObsParser((key, attributes) -> freq.get(), PeriodParsers::onStandardFreq, Parser.onDouble());

        Key.Builder key = Key.builder(asList("a", "b"));

        freq.set(Frequency.ANNUAL);
        p.frequency(key, UnaryOperator.identity());
        assertThat(p.period("2001").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-01").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-A1").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-02").parsePeriod()).isNull();
        assertThat(p.period("2001-01-01").parsePeriod()).isNull();
        assertThat(p.period("hello").parsePeriod()).isNull();
        assertThat(p.period("").parsePeriod()).isNull();

        freq.set(Frequency.HALF_YEARLY);
        p.frequency(key, UnaryOperator.identity());
        assertThat(p.period("2001-01").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-07").parsePeriod()).isEqualTo("2001-07-01T00:00:00");
        assertThat(p.period("2001-S1").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-S2").parsePeriod()).isEqualTo("2001-07-01T00:00:00");
        assertThat(p.period("2001S2").parsePeriod()).isEqualTo("2001-07-01T00:00:00");
        assertThat(p.period("2001S0").parsePeriod()).isNull();
        assertThat(p.period("2001S3").parsePeriod()).isNull();
        assertThat(p.period("hello").parsePeriod()).isNull();
        assertThat(p.period("").parsePeriod()).isNull();

        freq.set(Frequency.QUARTERLY);
        p.frequency(key, UnaryOperator.identity());
        assertThat(p.period("2001-01").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-04").parsePeriod()).isEqualTo("2001-04-01T00:00:00");
        assertThat(p.period("2001-Q1").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-Q2").parsePeriod()).isEqualTo("2001-04-01T00:00:00");
        assertThat(p.period("2001Q2").parsePeriod()).isEqualTo("2001-04-01T00:00:00");
        assertThat(p.period("2001-Q0").parsePeriod()).isNull();
        assertThat(p.period("hello").parsePeriod()).isNull();
        assertThat(p.period("").parsePeriod()).isNull();

        freq.set(Frequency.MONTHLY);
        p.frequency(key, UnaryOperator.identity());
        assertThat(p.period("2001-01").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-02").parsePeriod()).isEqualTo("2001-02-01T00:00:00");
        assertThat(p.period("2001-M1").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001M1").parsePeriod()).isEqualTo("2001-01-01T00:00:00");
        assertThat(p.period("2001-M0").parsePeriod()).isNull();
        assertThat(p.period("hello").parsePeriod()).isNull();
        assertThat(p.period("").parsePeriod()).isNull();
    }
}
