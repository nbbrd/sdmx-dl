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
package internal.util.rest;

import static internal.util.rest.RestClientImpl.isDowngradingProtocolOnRedirect;
import java.net.MalformedURLException;
import java.net.URL;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class RestClientImplTest {

    @Test
    public void testIsDowngradingProtocolOnRedirect() throws MalformedURLException {
        assertThat(isDowngradingProtocolOnRedirect(new URL("http://here"), new URL("http://there"))).isFalse();
        assertThat(isDowngradingProtocolOnRedirect(new URL("https://here"), new URL("http://there"))).isTrue();
        assertThat(isDowngradingProtocolOnRedirect(new URL("http://here"), new URL("https://there"))).isFalse();
    }
}
