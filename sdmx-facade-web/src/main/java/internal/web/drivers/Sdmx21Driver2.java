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
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.parser.DataFactory;
import static internal.web.SdmxWebProperty.*;
import be.nbb.sdmx.facade.web.SdmxWebSource;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.util.drivers.SdmxWebResource;
import internal.web.SdmxWebDriverSupport;
import internal.web.SdmxWebClient;
import internal.web.SdmxWebProperty;

/**
 *
 * @author Philippe Charles
 */
public final class Sdmx21Driver2 implements SdmxWebDriver {

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name("sdmx21@facade")
            .client(Sdmx21Driver2::of)
            .supportedProperties(Util.CONNECTION_PROPERTIES)
            .supportedProperty(SERIES_KEYS_ONLY_SUPPORTED_PROPERTY)
            .sources(SdmxWebResource.load("/internal/web/drivers/sdmx21.xml"))
            .build();

    private static SdmxWebClient of(SdmxWebSource s, LanguagePriorityList l, SdmxWebBridge b) {
        return new AbstractSdmx21(
                s.getEndpoint(),
                l,
                Util.getRestClient(s, b),
                SdmxWebProperty.isSeriesKeysOnlySupported(s.getProperties()),
                DataFactory.sdmx21()
        );
    }
}
