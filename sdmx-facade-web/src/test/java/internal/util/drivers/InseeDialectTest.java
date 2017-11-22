/*
 * Copyright 2017 National Bank of Belgium
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
package internal.util.drivers;

import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dimension;
import static be.nbb.sdmx.facade.Frequency.*;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.util.Chars;
import java.util.function.Function;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import test.DialectAssertions;

/**
 *
 * @author Philippe Charles
 */
public class InseeDialectTest {

    @Test
    public void testCompliance() {
        DialectAssertions.assertDialectCompliance(new InseeDialect());
    }

    @Test
    public void testFreqParser() {
        InseeDialect d = new InseeDialect();
        DataStructure dsd = DataStructure.builder()
                .dimension(Dimension.builder().id("FREQ").position(1).label("").build())
                .ref(DataStructureRef.parse("abc"))
                .label("")
                .build();
        Key.Builder key = Key.builder(dsd);
        assertThat(d.getFreqParser(dsd).parse(key.put("FREQ", "A"), Function.identity())).isEqualTo(ANNUAL);
        assertThat(d.getFreqParser(dsd).parse(key.put("FREQ", "T"), Function.identity())).isEqualTo(QUARTERLY);
        assertThat(d.getFreqParser(dsd).parse(key.put("FREQ", "M"), Function.identity())).isEqualTo(MONTHLY);
        assertThat(d.getFreqParser(dsd).parse(key.put("FREQ", "B"), Function.identity())).isEqualTo(MONTHLY);
        assertThat(d.getFreqParser(dsd).parse(key.put("FREQ", "S"), Function.identity())).isEqualTo(HALF_YEARLY);
        assertThat(d.getFreqParser(dsd).parse(key.put("FREQ", "X"), Function.identity())).isEqualTo(UNDEFINED);
    }

    @Test
    public void testPeriodParser() {
        InseeDialect d = new InseeDialect();
        assertThat(d.getPeriodParser(ANNUAL).parse("2013")).isEqualTo("2013-01-01T00:00:00");
        assertThat(d.getPeriodParser(QUARTERLY).parse("2014-Q3")).isEqualTo("2014-07-01T00:00:00");
        assertThat(d.getPeriodParser(MONTHLY).parse("1990-09")).isEqualTo("1990-09-01T00:00:00");
        assertThat(d.getPeriodParser(HALF_YEARLY).parse("2012-S2")).isEqualTo("2012-07-01T00:00:00");
        assertThat(d.getPeriodParser(MINUTELY).parse("2012-S2")).isNull();
    }

    @Test
    @SuppressWarnings("null")
    public void testValueParser() {
        Chars.Parser<Double> p = new InseeDialect().getValueParser();
        assertThat(p.parse("hello")).isNull();
        assertThat(p.parse("3.14")).isEqualTo(3.14);
        assertThatNullPointerException().isThrownBy(() -> p.parse(null));
    }
}
