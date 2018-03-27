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
package internal.web.sdmx21;

import be.nbb.sdmx.facade.web.spi.SdmxWebBridge;
import internal.util.rest.RestClientImpl;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.util.Property;
import static be.nbb.sdmx.facade.web.SdmxWebProperty.*;
import be.nbb.sdmx.facade.web.SdmxWebEntryPoint;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.web.WebDriverSupport;
import java.net.MalformedURLException;
import java.net.URL;
import internal.web.WebClient;
import internal.util.rest.RestClient;

/**
 *
 * @author Philippe Charles
 */
public final class Sdmx21Driver2 implements SdmxWebDriver {

    private static final String PREFIX = "sdmx:sdmx21:";

    @lombok.experimental.Delegate
    private final WebDriverSupport support = WebDriverSupport
            .builder()
            .prefix(PREFIX)
            .client(Sdmx21Driver2::of)
            .build();

    private static WebClient of(SdmxWebEntryPoint o, String prefix, LanguagePriorityList langs, SdmxWebBridge bridge) {
        return Sdmx21RestClient.of(getEndPoint(o, prefix), isSeriesKeysOnly(o), langs, getRestClient(o, bridge));
    }

    private static URL getEndPoint(SdmxWebEntryPoint o, String prefix) {
        try {
            return new URL(o.getUri().toString().substring(prefix.length()));
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static boolean isSeriesKeysOnly(SdmxWebEntryPoint o) {
        return Property.get(SERIES_KEYS_ONLY_SUPPORTED_PROPERTY, DEFAULT_SERIES_KEYS_ONLY_SUPPORTED, o.getProperties());
    }

    private static RestClient getRestClient(SdmxWebEntryPoint o, SdmxWebBridge bridge) {
        return RestClientImpl.of(
                Property.get(READ_TIMEOUT_PROPERTY, DEFAULT_READ_TIMEOUT, o.getProperties()),
                Property.get(CONNECT_TIMEOUT_PROPERTY, DEFAULT_CONNECT_TIMEOUT, o.getProperties()),
                Property.get(MAX_REDIRECTS_PROPERTY, DEFAULT_MAX_REDIRECTS, o.getProperties()),
                bridge.getProxySelector(o), bridge.getSslSocketFactory(o)
        );
    }
}
