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
package internal.sdmxdl.ri.web.drivers;

import internal.sdmxdl.ri.web.RestClients;
import internal.sdmxdl.ri.web.Sdmx21RestClient;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;
import sdmxdl.util.parser.DataFactories;
import sdmxdl.util.SdmxFix;
import static sdmxdl.util.SdmxFix.Category.QUERY;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebDriver;
import sdmxdl.util.web.SdmxWebDriverSupport;
import java.io.IOException;
import java.net.URL;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.util.web.DataRequest;
import sdmxdl.util.web.SdmxWebClient;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(SdmxWebDriver.class)
public final class WbDriver2 implements SdmxWebDriver {

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name("web-ri:wb")
            .rank(NATIVE_RANK)
            .client(WbClient2::new)
            .supportedProperties(RestClients.CONNECTION_PROPERTIES)
            .sourceOf("WB", "World Bank", "https://api.worldbank.org/v2/sdmx/rest")
            .build();

    private static final class WbClient2 extends Sdmx21RestClient {

        private WbClient2(SdmxWebSource s, SdmxWebContext c) {
            super(SdmxWebClient.getClientName(s), s.getEndpoint(), c.getLanguages(), RestClients.getRestClient(s, c), true, DataFactories.SDMX21);
        }

        @SdmxFix(id = 1, category = QUERY, cause = "'/' separator required at the end of query")
        private static final String SEP = "";

        @Override
        protected URL getFlowsQuery() throws IOException {
            return getFlowsQuery(endpoint).path(SEP).build();
        }

        @Override
        protected URL getFlowQuery(DataflowRef ref) throws IOException {
            return getFlowQuery(endpoint, ref).path(SEP).build();
        }

        @Override
        protected URL getStructureQuery(DataStructureRef ref) throws IOException {
            return getStructureQuery(endpoint, ref).path(SEP).build();
        }

        @Override
        protected URL getDataQuery(DataRequest request) throws IOException {
            return getDataQuery(endpoint, request).path(SEP).build();
        }
    }
}
