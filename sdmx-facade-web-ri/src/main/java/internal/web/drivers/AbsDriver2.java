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

import be.nbb.sdmx.facade.DataFilter;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.util.SdmxFix;
import be.nbb.sdmx.facade.web.SdmxWebSource;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.web.SdmxWebDriverSupport;
import java.io.IOException;
import java.net.URL;
import be.nbb.sdmx.facade.web.spi.SdmxWebContext;

/**
 *
 * @author Philippe Charles
 */
public final class AbsDriver2 implements SdmxWebDriver {

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name("abs@facade")
            .client(AbsClient2::new)
            .supportedProperties(Util.CONNECTION_PROPERTIES)
            .sourceOf("ABS", "Australian Bureau of Statistics", "http://stat.data.abs.gov.au/restsdmx/sdmx.ashx")
            .build();

    private static final class AbsClient2 extends AbstractDotStat {

        private AbsClient2(SdmxWebSource s, LanguagePriorityList l, SdmxWebContext c) {
            super(s.getEndpoint(), l, Util.getRestClient(s, c));
        }

        @SdmxFix(id = "ABS#1", cause = "Agency is required in query")
        private static final String AGENCY = "ABS";

        @Override
        protected URL getStructureQuery(DataStructureRef ref) throws IOException {
            return getStructureQuery(endpoint, ref).path(AGENCY).build();
        }

        @Override
        protected URL getDataQuery(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
            return getDataQuery(endpoint, flowRef, key, filter).path(AGENCY).build();
        }
    }
}
