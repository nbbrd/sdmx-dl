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

import static nbbrd.io.text.BaseProperty.keysOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static sdmxdl.Languages.ANY;
import static sdmxdl.provider.connectors.drivers.Connectors.NEEDS_CREDENTIALS_PROPERTY;
import static sdmxdl.provider.connectors.drivers.Sdmx20Driver.CONNECTORS_SDMX_20;
import static sdmxdl.provider.web.DriverProperties.*;

/**
 * @author Philippe Charles
 */
public class Sdmx20DriverTest {

    @Test
    public void testCompliance() {
        DriverAssert.assertCompliance(new Sdmx20Driver());
    }

    @Test
    public void testProperties() {
        assertThat(new Sdmx20Driver().getDriverPropertyNames())
                .containsExactlyInAnyOrderElementsOf(
                        keysOf(
                                CONNECT_TIMEOUT_PROPERTY,
                                READ_TIMEOUT_PROPERTY,
                                MAX_REDIRECTS_PROPERTY,
                                CACHE_TTL_PROPERTY,
                                NEEDS_CREDENTIALS_PROPERTY)
                );
    }

    @Test
    public void testConnect() {
        WebSource x = WebSource.builder().id("localhost").driver(CONNECTORS_SDMX_20).endpointOf("http://localhost").build();

        assertThatCode(() -> new Sdmx20Driver().connect(x, ANY, DriverAssert.noOpWebContext()).close())
                .doesNotThrowAnyException();
    }
}
