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
package internal.connectors;

import it.bancaditalia.oss.sdmx.exceptions.SdmxExceptionFactory;
import java.net.HttpURLConnection;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class UtilTest {

    @Test
    public void testIsNoResultMatchingQuery() {
        assertThat(Util.isNoResultMatchingQuery(SdmxExceptionFactory.createRestException(HttpURLConnection.HTTP_NOT_FOUND, null, null))).isTrue();
        assertThat(Util.isNoResultMatchingQuery(SdmxExceptionFactory.createRestException(HttpURLConnection.HTTP_BAD_REQUEST, null, null))).isFalse();
    }
}
