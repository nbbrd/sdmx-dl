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

import org.junit.jupiter.api.Test;
import sdmxdl.web.WebSource;
import tests.sdmxdl.web.spi.DriverAssert;

import static org.assertj.core.api.Assertions.assertThatCode;
import static sdmxdl.Languages.ANY;
import static sdmxdl.provider.connectors.drivers.Sdmx20Driver.CONNECTORS_SDMX_20;

/**
 * @author Philippe Charles
 */
public class Sdmx20DriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new Sdmx20Driver());
    }

    @Test
    public void testConnect() {
        WebSource x = WebSource.builder().id("localhost").driver(CONNECTORS_SDMX_20).endpointOf("http://localhost").build();

        assertThatCode(() -> new Sdmx20Driver().connect(x, ANY, DriverAssert.noOpWebContext()).close())
                .doesNotThrowAnyException();
    }
}
