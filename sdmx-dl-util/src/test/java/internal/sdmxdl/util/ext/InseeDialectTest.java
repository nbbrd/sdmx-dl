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
package internal.sdmxdl.util.ext;

import _test.sdmxdl.util.DialectAssertions;
import org.junit.Test;
import sdmxdl.DataStructure;
import sdmxdl.DataStructureRef;
import sdmxdl.Dimension;
import sdmxdl.Key;

import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.Frequency.*;

/**
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
                .primaryMeasureId("")
                .label("")
                .build();
        Key.Builder key = Key.builder(dsd);
        assertThat(InseeDialect.getFreqFactory(dsd).apply(key.put("FREQ", "A"), UnaryOperator.identity())).isEqualTo(ANNUAL);
        assertThat(InseeDialect.getFreqFactory(dsd).apply(key.put("FREQ", "T"), UnaryOperator.identity())).isEqualTo(QUARTERLY);
        assertThat(InseeDialect.getFreqFactory(dsd).apply(key.put("FREQ", "M"), UnaryOperator.identity())).isEqualTo(MONTHLY);
        assertThat(InseeDialect.getFreqFactory(dsd).apply(key.put("FREQ", "B"), UnaryOperator.identity())).isEqualTo(MONTHLY);
        assertThat(InseeDialect.getFreqFactory(dsd).apply(key.put("FREQ", "S"), UnaryOperator.identity())).isEqualTo(HALF_YEARLY);
        assertThat(InseeDialect.getFreqFactory(dsd).apply(key.put("FREQ", "X"), UnaryOperator.identity())).isEqualTo(UNDEFINED);
    }

    @Test
    public void testPeriodParser() {
        InseeDialect d = new InseeDialect();
        assertThat(InseeDialect.getPeriodParser(ANNUAL).parse("2013")).isEqualTo("2013-01-01T00:00:00");
        assertThat(InseeDialect.getPeriodParser(QUARTERLY).parse("2014-Q3")).isEqualTo("2014-07-01T00:00:00");
        assertThat(InseeDialect.getPeriodParser(MONTHLY).parse("1990-09")).isEqualTo("1990-09-01T00:00:00");
        assertThat(InseeDialect.getPeriodParser(HALF_YEARLY).parse("2012-S2")).isEqualTo("2012-07-01T00:00:00");
        assertThat(InseeDialect.getPeriodParser(MINUTELY).parse("2012-S2")).isNull();
    }
}
