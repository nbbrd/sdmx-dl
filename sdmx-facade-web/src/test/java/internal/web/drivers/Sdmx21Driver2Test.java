/*
 * Copyright 2018 National Bank of Belgium
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
package internal.web.drivers;

import _test.DriverAssertions;
import be.nbb.sdmx.facade.web.SdmxWebSource;
import internal.connectors.drivers.Sdmx21Driver;
import java.util.Comparator;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class Sdmx21Driver2Test {

    @Test
    public void testCompliance() {
        DriverAssertions.assertDriverCompliance(new Sdmx21Driver2());
    }

    @Test
    public void testEquivalence() {
        assertThat(new Sdmx21Driver2().getDefaultSources())
                .usingElementComparator(Comparator.comparing(SdmxWebSource::getName))
                .containsExactlyElementsOf(new Sdmx21Driver().getDefaultSources());
    }
}
