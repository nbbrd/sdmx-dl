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
package _test;

import static be.nbb.sdmx.facade.LanguagePriorityList.ANY;
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.web.SdmxWebSource;
import be.nbb.sdmx.facade.web.spi.SdmxWebBridge;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class DriverAssertions {

    @SuppressWarnings("null")
    public void assertDriverCompliance(SdmxWebDriver d) {
        if (d instanceof HasCache) {
            CacheAssertions.assertCacheBehavior((HasCache) d);
        }

        SdmxWebSource validSource = SdmxWebSource
                .builder()
                .name("valid")
                .driver(d.getName())
                .endpointOf("http://localhost")
                .build();

        SdmxWebSource invalidSource = validSource.toBuilder().driver("").build();

        assertThat(d.getName()).isNotBlank();

        assertThatNullPointerException().isThrownBy(() -> d.connect(null, ANY, SdmxWebBridge.getDefault()));
        assertThatNullPointerException().isThrownBy(() -> d.connect(validSource, null, SdmxWebBridge.getDefault()));
        assertThatNullPointerException().isThrownBy(() -> d.connect(validSource, ANY, null));

        assertThatIllegalArgumentException().isThrownBy(() -> d.connect(invalidSource, ANY, SdmxWebBridge.getDefault()));

        assertThat(d.getDefaultSources())
                .allSatisfy(o -> checkSource(o))
                .allMatch(o -> o.getDriver().equals(d.getName()));

        assertThat(d.getClass()).isFinal();
    }

    private void checkSource(SdmxWebSource o) {
        assertThat(o.getName()).isNotBlank();
        assertThat(o.getDescription()).isNotBlank();
        assertThat(o.getProperties()).isNotNull();
    }
}
