/*
 * Copyright 2020 National Bank of Belgium
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
package sdmxdl.web;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("ConstantConditions")
public class SdmxWebSourceTest {

    @Test
    public void testBuilderEndpointOf() throws MalformedURLException {
        assertThatNullPointerException()
                .isThrownBy(() -> SdmxWebSource.builder().endpointOf(null));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> SdmxWebSource.builder().endpointOf("h ttp://localhost"));

        assertThat(SdmxWebSource.builder().endpointOf("http://localhost").name("").description("").driver("").build().getEndpoint())
                .isEqualTo(new URL("http://localhost"));
    }

    @Test
    public void testBuilderPropertyOf() {
        assertThatNullPointerException()
                .isThrownBy(() -> SdmxWebSource.builder().propertyOf(null, ""));

        assertThatNullPointerException()
                .isThrownBy(() -> SdmxWebSource.builder().propertyOf("", null));

        assertThat(SdmxWebSource.builder().propertyOf("hello", "world").endpointOf("http://localhost").name("").description("").driver("").build().getProperties())
                .containsEntry("hello", "world");
    }
}
