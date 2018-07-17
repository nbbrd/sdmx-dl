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

import be.nbb.sdmx.facade.parser.DataFactory;
import static internal.web.SdmxWebProperty.*;
import be.nbb.sdmx.facade.web.SdmxWebSource;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.util.drivers.SdmxWebResource;
import internal.web.SdmxWebDriverSupport;
import internal.web.SdmxWebClient;
import internal.web.SdmxWebProperty;
import be.nbb.sdmx.facade.web.spi.SdmxWebContext;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxWebDriver.class)
public final class Sdmx21Driver2 implements SdmxWebDriver {

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name("web-ri:sdmx21")
            .rank(NATIVE_RANK)
            .client(Sdmx21Driver2::of)
            .supportedProperties(Util.CONNECTION_PROPERTIES)
            .supportedProperty(SERIES_KEYS_ONLY_SUPPORTED_PROPERTY)
            .sources(SdmxWebResource.load("/internal/web/drivers/sdmx21.xml"))
            .build();

    private static SdmxWebClient of(SdmxWebSource s, SdmxWebContext c) {
        return new Sdmx21RestClient(
                SdmxWebClient.getClientName(s),
                s.getEndpoint(),
                c.getLanguages(),
                Util.getRestClient(s, c),
                SdmxWebProperty.isSeriesKeysOnlySupported(s.getProperties()),
                DataFactory.sdmx21()
        );
    }
}
