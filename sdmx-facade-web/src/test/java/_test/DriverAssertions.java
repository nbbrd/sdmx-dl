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

import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.web.SdmxWebSource;
import be.nbb.sdmx.facade.web.spi.SdmxWebBridge;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.web.SdmxWebDriverSupport;
import ioutil.IO;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class DriverAssertions {

    @SuppressWarnings("null")
    public void assertDriverCompliance(SdmxWebDriver d, String prefix) {
        if (d instanceof HasCache) {
            CacheAssertions.assertCacheBehavior((HasCache) d);
        }

        assertThatNullPointerException().isThrownBy(() -> d.accepts(null));
        assertThatNullPointerException().isThrownBy(() -> d.connect(null, LanguagePriorityList.ANY, SdmxWebBridge.getDefault()));
        assertThatNullPointerException().isThrownBy(() -> d.connect(SdmxWebSource.builder().name("").build(), null, SdmxWebBridge.getDefault()));
        assertThatNullPointerException().isThrownBy(() -> d.connect(SdmxWebSource.builder().name("").build(), LanguagePriorityList.ANY, null));

        assertThat(d.getDefaultSources())
                .allSatisfy(o -> checkSource(o, prefix))
                .allMatch(IO.Predicate.unchecked(d::accepts));

        assertThat(d.getClass()).isFinal();
    }

    private void checkSource(SdmxWebSource o, String prefix) {
        assertThat(o.getName()).isNotBlank();
        assertThat(o.getDescription()).isNotBlank();
        assertThat(o.getProperties()).isNotNull();
        assertThat(o.getUri().toString()).startsWith(prefix);
        assertThatCode(() -> SdmxWebDriverSupport.getEndpoint(o, prefix).toURL()).doesNotThrowAnyException();
    }
}
