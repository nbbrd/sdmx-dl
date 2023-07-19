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
package sdmxdl.provider.connectors.drivers;

import sdmxdl.provider.connectors.drivers.Sdmx20Driver;
import org.junit.jupiter.api.Test;
import sdmxdl.web.SdmxWebSource;
import tests.sdmxdl.web.WebDriverAssert;

import static org.assertj.core.api.Assertions.assertThatCode;
import static sdmxdl.Languages.ANY;

/**
 * @author Philippe Charles
 */
public class Sdmx20DriverTest {

    @Test
    public void testCompliance() {
        WebDriverAssert.assertCompliance(new Sdmx20Driver());
    }

    @Test
    public void testConnect() {
        SdmxWebSource x = SdmxWebSource.builder().id("localhost").driver("connectors:sdmx20").endpointOf("http://localhost").build();

        assertThatCode(() -> new Sdmx20Driver().connect(x, ANY, WebDriverAssert.noOpWebContext()).close())
                .doesNotThrowAnyException();
    }
}
