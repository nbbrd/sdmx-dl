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

import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.parser.DataFactory;
import be.nbb.sdmx.facade.util.SdmxFix;
import be.nbb.sdmx.facade.web.SdmxWebSource;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.web.SdmxWebDriverSupport;
import java.io.IOException;
import java.net.URL;
import be.nbb.sdmx.facade.web.spi.SdmxWebContext;
import internal.web.DataRequest;

/**
 *
 * @author Philippe Charles
 */
public final class WbDriver2 implements SdmxWebDriver {

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name("wb@facade")
            .client(WbClient2::new)
            .supportedProperties(Util.CONNECTION_PROPERTIES)
            .sourceOf("WB", "World Bank", "https://api.worldbank.org/v2/sdmx/rest")
            .build();

    private static final class WbClient2 extends Sdmx21RestClient {

        private WbClient2(SdmxWebSource s, LanguagePriorityList l, SdmxWebContext c) {
            super(s.getEndpoint(), l, Util.getRestClient(s, c), true, DataFactory.sdmx21());
        }

        @SdmxFix(id = "WB#1", cause = "'/' separator required at the end of query")
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
