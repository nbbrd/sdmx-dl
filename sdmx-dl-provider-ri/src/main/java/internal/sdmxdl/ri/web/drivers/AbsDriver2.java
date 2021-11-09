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

import internal.sdmxdl.ri.web.DotStatRestParsers;
import internal.sdmxdl.ri.web.DotStatRestQueries;
import internal.sdmxdl.ri.web.RiHttpUtils;
import internal.sdmxdl.ri.web.RiRestClient;
import internal.util.http.URLQueryBuilder;
import nbbrd.design.VisibleForTesting;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataStructureRef;
import sdmxdl.util.SdmxFix;
import sdmxdl.DataRef;
import sdmxdl.util.web.SdmxRestDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.IOException;
import java.net.URL;

import static sdmxdl.util.SdmxFix.Category.QUERY;

/**
 * @author Philippe Charles
 */
@ServiceProvider(SdmxWebDriver.class)
public final class AbsDriver2 implements SdmxWebDriver {

    private static final String RI_ABS = "ri:abs";

    @lombok.experimental.Delegate
    private final SdmxRestDriverSupport support = SdmxRestDriverSupport
            .builder()
            .name(RI_ABS)
            .rank(NATIVE_RANK)
            .client(AbsDriver2::newClient)
            .supportedProperties(RiHttpUtils.CONNECTION_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .name("ABS")
                    .description("Australian Bureau of Statistics")
                    .driver(RI_ABS)
                    .endpointOf("https://stat.data.abs.gov.au/restsdmx/sdmx.ashx")
                    .websiteOf("https://stat.data.abs.gov.au")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:ABS")
                    .build())
            .build();

    private static RiRestClient newClient(SdmxWebSource s, SdmxWebContext c) throws IOException {
        return RiRestClient.of(s, c, "SDMX20", new AbsQueries(), new DotStatRestParsers(), false);
    }

    @VisibleForTesting
    static final class AbsQueries extends DotStatRestQueries {

        @SdmxFix(id = 1, category = QUERY, cause = "Agency is required in query")
        private static final String AGENCY = "ABS";

        @Override
        public URLQueryBuilder getStructureQuery(URL endpoint, DataStructureRef ref) {
            return super.getStructureQuery(endpoint, ref).path(AGENCY);
        }

        @Override
        public URLQueryBuilder getDataQuery(URL endpoint, DataRef ref) {
            return super.getDataQuery(endpoint, ref).path(AGENCY);
        }
    }
}
