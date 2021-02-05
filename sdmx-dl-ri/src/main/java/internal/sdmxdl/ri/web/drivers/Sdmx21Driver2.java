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
package internal.sdmxdl.ri.web.drivers;

import internal.sdmxdl.ri.web.RestClients;
import internal.sdmxdl.ri.web.Sdmx21RestClient;
import nbbrd.io.function.IOSupplier;
import nbbrd.service.ServiceProvider;
import sdmxdl.util.parser.ObsFactories;
import sdmxdl.util.web.SdmxWebClient;
import sdmxdl.util.web.SdmxWebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;
import sdmxdl.xml.XmlWebSource;

import java.io.IOException;
import java.util.List;

import static sdmxdl.util.web.SdmxWebProperty.DETAIL_SUPPORTED_PROPERTY;
import static sdmxdl.util.web.SdmxWebProperty.TRAILING_SLASH_REQUIRED_PROPERTY;

/**
 * @author Philippe Charles
 */
@ServiceProvider(SdmxWebDriver.class)
public final class Sdmx21Driver2 implements SdmxWebDriver {

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name("ri:sdmx21")
            .rank(NATIVE_RANK)
            .client(Sdmx21Driver2::of)
            .supportedProperties(RestClients.CONNECTION_PROPERTIES)
            .supportedProperty(DETAIL_SUPPORTED_PROPERTY.getKey())
            .supportedProperty(TRAILING_SLASH_REQUIRED_PROPERTY.getKey())
            .sources(IOSupplier.unchecked(Sdmx21Driver2::getSources).get())
            .build();

    private static List<SdmxWebSource> getSources() throws IOException {
        return XmlWebSource.getParser().parseResource(Sdmx21Driver2.class, "ri-sdmx21.xml");
    }

    private static SdmxWebClient of(SdmxWebSource s, SdmxWebContext c) throws IOException {
        return new Sdmx21RestClient(
                SdmxWebClient.getClientName(s),
                s.getEndpoint(),
                c.getLanguages(),
                RestClients.getRestClient(s, c),
                DETAIL_SUPPORTED_PROPERTY.get(s.getProperties()),
                TRAILING_SLASH_REQUIRED_PROPERTY.get(s.getProperties()),
                ObsFactories.getObsFactory(c, s, "SDMX21")
        );
    }
}
