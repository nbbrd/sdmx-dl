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
package internal.web.drivers;

import be.nbb.sdmx.facade.web.spi.SdmxWebBridge;
import internal.util.rest.RestClientImpl;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.util.Property;
import static be.nbb.sdmx.facade.web.SdmxWebProperty.*;
import be.nbb.sdmx.facade.web.SdmxWebSource;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.web.SdmxWebDriverSupport;
import internal.util.rest.RestClient;
import internal.web.SdmxWebClient;

/**
 *
 * @author Philippe Charles
 */
public final class Sdmx21Driver2 implements SdmxWebDriver {

    private static final String NAME = "sdmx21";

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name("sdmx21")
            .client(Sdmx21Driver2::of)
            .source(SdmxWebSource
                    .builder()
                    .name("ECB")
                    .description("European Central Bank")
                    .driver(NAME)
                    .endpointOf("https://sdw-wsrest.ecb.europa.eu/service")
                    .propertyOf(SERIES_KEYS_ONLY_SUPPORTED_PROPERTY, true)
                    .build())
            .build();

    private static SdmxWebClient of(SdmxWebSource o, LanguagePriorityList langs, SdmxWebBridge bridge) {
        return Sdmx21RestClient.of(o.getEndpoint(), isSeriesKeysOnly(o), langs, getRestClient(o, bridge));
    }

    private static boolean isSeriesKeysOnly(SdmxWebSource o) {
        return Property.get(SERIES_KEYS_ONLY_SUPPORTED_PROPERTY, DEFAULT_SERIES_KEYS_ONLY_SUPPORTED, o.getProperties());
    }

    private static RestClient getRestClient(SdmxWebSource o, SdmxWebBridge bridge) {
        return RestClientImpl.of(
                Property.get(READ_TIMEOUT_PROPERTY, DEFAULT_READ_TIMEOUT, o.getProperties()),
                Property.get(CONNECT_TIMEOUT_PROPERTY, DEFAULT_CONNECT_TIMEOUT, o.getProperties()),
                Property.get(MAX_REDIRECTS_PROPERTY, DEFAULT_MAX_REDIRECTS, o.getProperties()),
                bridge.getProxySelector(o), bridge.getSslSocketFactory(o)
        );
    }
}
