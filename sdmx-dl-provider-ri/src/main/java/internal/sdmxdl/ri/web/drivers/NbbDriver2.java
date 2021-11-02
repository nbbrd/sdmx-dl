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
import internal.util.rest.InterceptingClient;
import internal.util.rest.MediaType;
import internal.util.rest.RestQueryBuilder;
import nbbrd.design.VisibleForTesting;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataFilter;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.LanguagePriorityList;
import sdmxdl.ext.ObsFactory;
import sdmxdl.util.SdmxFix;
import sdmxdl.util.parser.ObsFactories;
import sdmxdl.util.web.SdmxWebClient;
import sdmxdl.util.web.SdmxWebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.IOException;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;
import static java.util.Collections.emptyMap;
import static sdmxdl.util.SdmxFix.Category.PROTOCOL;
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
            .client(NbbDriver2::newClient)
            .supportedProperties(RestClients.CONNECTION_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .name("NBB")
                    .description("National Bank of Belgium")
                    .driver(RI_NBB)
                    .endpointOf("https://stat.nbb.be/restsdmx/sdmx.ashx")
                    .websiteOf("https://stat.nbb.be")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:NBB")
                    .build())
            .build();

    private static @NonNull RiRestClient newClient(@NonNull SdmxWebSource s, @NonNull SdmxWebContext c) throws IOException {
        return newClient(
                SdmxWebClient.getClientName(s),
                s.getEndpoint(),
                c.getLanguages(),
                ObsFactories.getObsFactory(c, s, "SDMX20"),
                HttpRest.newClient(RestClients.getRestContext(s, c))
        );
    }

    @VisibleForTesting
    static @NonNull RiRestClient newClient(@NonNull String name, @NonNull URL endpoint, @NonNull LanguagePriorityList langs, @NonNull ObsFactory obsFactory, HttpRest.@NonNull Client executor) {
        return new RiRestClient(name, endpoint, langs, obsFactory,
                new InterceptingClient(executor, (client, q, m, l, r) -> checkInternalErrorRedirect(r)),
                new NbbQueries(),
                new DotStatRestParsers(),
                false);
    }

    @SdmxFix(id = 2, category = PROTOCOL, cause = "Some internal errors redirect to an HTML page")
    static HttpRest.Response checkInternalErrorRedirect(HttpRest.Response result) throws IOException {
        if (result.getContentType().isCompatible(HTML_TYPE)) {
            throw SERVICE_UNAVAILABLE;
        }
        return result;
    }

    private static final MediaType HTML_TYPE = new MediaType("text", "html", emptyMap());
    private static final HttpRest.ResponseError SERVICE_UNAVAILABLE = new HttpRest.ResponseError(HTTP_UNAVAILABLE, "Service unavailable", emptyMap());

    @VisibleForTesting
    static final class NbbQueries extends DotStatRestQueries {

        @SdmxFix(id = 1, category = QUERY, cause = "'/all' must be encoded to '%2Fall'")
        @Override
        public RestQueryBuilder getDataQuery(URL endpoint, DataflowRef flowRef, Key key, DataFilter filter) {
            return RestQueryBuilder
                    .of(endpoint)
                    .path(DotStatRestQueries.DATA_RESOURCE)
                    .path(flowRef.getId())
                    .path(key + "/all")
                    .param("format", "compact_v2");
        }
    }
}
