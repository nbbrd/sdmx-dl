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
import internal.util.rest.RestQueryBuilder;
import nbbrd.io.Resource;
import nbbrd.service.ServiceProvider;
import sdmxdl.LanguagePriorityList;
import sdmxdl.ext.ObsFactory;
import sdmxdl.util.MediaType;
import sdmxdl.util.SdmxFix;
import sdmxdl.util.parser.ObsFactories;
import sdmxdl.util.web.DataRequest;
import sdmxdl.util.web.SdmxWebClient;
import sdmxdl.util.web.SdmxWebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static sdmxdl.util.SdmxFix.Category.CONTENT;
import static sdmxdl.util.SdmxFix.Category.QUERY;

/**
 * @author Philippe Charles
 */
@ServiceProvider(SdmxWebDriver.class)
public final class NbbDriver2 implements SdmxWebDriver {

    private static final String RI_NBB = "ri:nbb";

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name(RI_NBB)
            .rank(NATIVE_RANK)
            .client(NbbClient2::new)
            .supportedProperties(RestClients.CONNECTION_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .name("NBB")
                    .description("National Bank of Belgium")
                    .driver(RI_NBB)
                    .endpointOf("https://stat.nbb.be/restsdmx/sdmx.ashx")
                    .websiteOf("https://stat.nbb.be")
                    .monitorOf("UptimeRobot", "m783847132-29c6aa5b9cf34b10a6466571")
                    .build())
            .build();

    static final class NbbClient2 extends RiRestClient {

        NbbClient2(SdmxWebSource s, SdmxWebContext c) throws IOException {
            this(
                    SdmxWebClient.getClientName(s),
                    s.getEndpoint(),
                    c.getLanguages(),
                    RestClients.getRestClient(s, c),
                    ObsFactories.getObsFactory(c, s, "SDMX20")
            );
        }

        NbbClient2(String name, URL endpoint, LanguagePriorityList langs, HttpRest.Client executor, ObsFactory obsFactory) {
            super(name, endpoint, langs, obsFactory, executor, new DotStatRestQueries(), new DotStatRestParsers(), false);
        }

        @SdmxFix(id = 1, category = QUERY, cause = "'/all' must be encoded to '%2Fall'")
        @Override
        protected URL getDataQuery(DataRequest request) throws IOException {
            return RestQueryBuilder
                    .of(endpoint)
                    .path(DotStatRestQueries.DATA_RESOURCE)
                    .path(request.getFlowRef().getId())
                    .path(request.getKey() + "/all")
                    .param("format", "compact_v2")
                    .build();
        }

        @Override
        protected HttpRest.Response open(URL query, List<MediaType> mediaType) throws IOException {
            HttpRest.Response result = super.open(query, mediaType);
            try {
                checkInternalErrorRedirect(result);
            } catch (Throwable ex) {
                Resource.ensureClosed(ex, result);
                throw ex;
            }
            return result;
        }

        @SdmxFix(id = 2, category = CONTENT, cause = "Some internal errors redirect to an html page")
        static void checkInternalErrorRedirect(HttpRest.Response result) throws IOException {
            if (result.getContentType().contains("text/html")) {
                throw new HttpRest.ResponseError(HttpsURLConnection.HTTP_UNAVAILABLE, "Service unavailable", Collections.emptyMap());
            }
        }
    }
}