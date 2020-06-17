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
package _test.sdmxdl.ri;

import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebDriver;
import static org.assertj.core.api.Assertions.*;
import sdmxdl.web.spi.SdmxWebContext;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class DriverAssertions {

    @SuppressWarnings("null")
    public void assertDriverCompliance(SdmxWebDriver d) {
        SdmxWebSource validSource = SdmxWebSource
                .builder()
                .name("valid")
                .driver(d.getName())
                .endpointOf("http://localhost")
                .build();

        SdmxWebSource invalidSource = validSource.toBuilder().driver("").build();

        SdmxWebContext context = SdmxWebContext.builder().build();
        
        assertThat(d.getName()).isNotBlank();

        assertThatNullPointerException().isThrownBy(() -> d.connect(null, context));
        assertThatNullPointerException().isThrownBy(() -> d.connect(validSource, null));

        assertThatIllegalArgumentException().isThrownBy(() -> d.connect(invalidSource, context));

        assertThat(d.getDefaultSources()).allSatisfy(o -> checkSource(o, d));

        assertThat(d.getClass()).isFinal();
    }

    private void checkSource(SdmxWebSource o, SdmxWebDriver d) {
        assertThat(o.getName()).isNotBlank();
        assertThat(o.getDescription()).isNotBlank();
        assertThat(o.getProperties()).isNotNull();
        assertThat(o.getDriver()).isEqualTo(d.getName());
        assertThat(o.getProperties().keySet()).isSubsetOf(d.getSupportedProperties());
    }
}
