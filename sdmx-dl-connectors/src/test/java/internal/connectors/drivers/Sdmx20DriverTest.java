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
package internal.connectors.drivers;

import be.nbb.sdmx.facade.web.SdmxWebSource;
import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import static _test.DriverAssertions.*;
import be.nbb.sdmx.facade.web.spi.SdmxWebContext;

/**
 *
 * @author Philippe Charles
 */
public class Sdmx20DriverTest {

    @Test
    public void testCompliance() {
        assertDriverCompliance(new Sdmx20Driver());
    }

    @Test
    public void testConnect() throws IOException {
        SdmxWebSource x = SdmxWebSource.builder().name("localhost").driver("connectors:sdmx20").endpointOf("http://localhost").build();
        assertThatCode(() -> new Sdmx20Driver().connect(x, SdmxWebContext.builder().build()).close()).doesNotThrowAnyException();
    }
}
