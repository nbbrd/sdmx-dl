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
package internal.web.drivers;

import be.nbb.sdmx.facade.web.SdmxWebSource;
import be.nbb.sdmx.facade.web.spi.SdmxWebBridge;
import internal.util.rest.RestClient;
import internal.util.rest.RestClientImpl;
import internal.web.SdmxWebProperty;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class Util {

    public RestClient getRestClient(SdmxWebSource o, SdmxWebBridge bridge) {
        return RestClientImpl.of(
                SdmxWebProperty.getReadTimeout(o.getProperties()),
                SdmxWebProperty.getConnectTimeout(o.getProperties()),
                SdmxWebProperty.getMaxRedirects(o.getProperties()),
                bridge.getProxySelector(o), bridge.getSslSocketFactory(o)
        );
    }

    public final List<String> CONNECTION_PROPERTIES = Collections.unmodifiableList(
            Arrays.asList(
                    SdmxWebProperty.CONNECT_TIMEOUT_PROPERTY,
                    SdmxWebProperty.READ_TIMEOUT_PROPERTY,
                    SdmxWebProperty.MAX_REDIRECTS_PROPERTY
            ));
}
