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
import internal.sdmxdl.ri.web.RestClients;
import internal.sdmxdl.ri.web.RiRestClient;
import internal.util.rest.HttpRest;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataStructureRef;
import sdmxdl.LanguagePriorityList;
import sdmxdl.ext.ObsFactory;
import sdmxdl.util.SdmxFix;
import sdmxdl.util.parser.ObsFactories;
import sdmxdl.util.web.DataRequest;
import sdmxdl.util.web.SdmxWebClient;
import sdmxdl.util.web.SdmxWebDriverSupport;
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
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name(RI_ABS)
            .rank(NATIVE_RANK)
            .client(AbsClient2::new)
            .supportedProperties(RestClients.CONNECTION_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .name("ABS")
                    .description("Australian Bureau of Statistics")
                    .driver(RI_ABS)
                    .endpointOf("http://stat.data.abs.gov.au/restsdmx/sdmx.ashx")
                    .websiteOf("http://stat.data.abs.gov.au")
                    .monitorOf("UptimeRobot", "m783847060-975767bc3a033ea3f3ac8ca2")
                    .build())
            .build();

    private static final class AbsClient2 extends RiRestClient {

        AbsClient2(SdmxWebSource s, SdmxWebContext c) throws IOException {
            this(
                    SdmxWebClient.getClientName(s),
                    s.getEndpoint(),
                    c.getLanguages(),
                    RestClients.getRestClient(s, c),
                    ObsFactories.getObsFactory(c, s, "SDMX20")
            );
        }

        AbsClient2(String name, URL endpoint, LanguagePriorityList langs, HttpRest.Client executor, ObsFactory obsFactory) {
            super(name, endpoint, langs, obsFactory, executor, new DotStatRestQueries(), new DotStatRestParsers(), false);
        }

        @SdmxFix(id = 1, category = QUERY, cause = "Agency is required in query")
        private static final String AGENCY = "ABS";

        @Override
        protected URL getStructureQuery(DataStructureRef ref) throws IOException {
            return queries.getStructureQuery(endpoint, ref).path(AGENCY).build();
        }

        @Override
        protected URL getDataQuery(DataRequest request) throws IOException {
            return queries.getDataQuery(endpoint, request.getFlowRef(), request.getKey(), request.getFilter()).path(AGENCY).build();
        }
    }
}