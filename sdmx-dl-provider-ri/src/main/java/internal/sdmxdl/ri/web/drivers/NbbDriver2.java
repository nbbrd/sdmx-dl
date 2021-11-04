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
import internal.util.http.*;
import internal.util.http.ext.InterceptingClient;
import nbbrd.design.VisibleForTesting;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.LanguagePriorityList;
import sdmxdl.ext.ObsFactory;
import sdmxdl.util.SdmxFix;
import sdmxdl.util.parser.ObsFactories;
import sdmxdl.DataRef;
import sdmxdl.util.web.SdmxRestDriverSupport;
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
    private final SdmxRestDriverSupport support = SdmxRestDriverSupport
            .builder()
            .name(RI_NBB)
            .rank(NATIVE_RANK)
            .client(NbbDriver2::newClient)
            .supportedProperties(RiHttpUtils.CONNECTION_PROPERTIES)
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

    private static RiRestClient newClient(SdmxWebSource s, SdmxWebContext c) throws IOException {
        return newClient(
                s.getId(),
                s.getEndpoint(),
                c.getLanguages(),
                ObsFactories.getObsFactory(c, s, "SDMX20"),
                RiHttpUtils.newClient(s, c)
        );
    }

    @VisibleForTesting
    static @NonNull RiRestClient newClient(@NonNull String name, @NonNull URL endpoint, @NonNull LanguagePriorityList langs, @NonNull ObsFactory obsFactory, @NonNull HttpClient executor) {
        return new RiRestClient(name, endpoint, langs, obsFactory,
                new InterceptingClient(executor, (client, request, response) -> checkInternalErrorRedirect(response)),
                new NbbQueries(),
                new DotStatRestParsers(),
                false);
    }

    @SdmxFix(id = 2, category = PROTOCOL, cause = "Some internal errors redirect to an HTML page")
    static HttpResponse checkInternalErrorRedirect(HttpResponse result) throws IOException {
        if (result.getContentType().isCompatible(HTML_TYPE)) {
            throw SERVICE_UNAVAILABLE;
        }
        return result;
    }

    private static final MediaType HTML_TYPE = new MediaType("text", "html", emptyMap());
    private static final HttpResponseException SERVICE_UNAVAILABLE = new HttpResponseException(HTTP_UNAVAILABLE, "Service unavailable");

    @VisibleForTesting
    static final class NbbQueries extends DotStatRestQueries {

        @SdmxFix(id = 1, category = QUERY, cause = "'/all' must be encoded to '%2Fall'")
        @Override
        public URLQueryBuilder getDataQuery(URL endpoint, DataRef ref) {
            return URLQueryBuilder
                    .of(endpoint)
                    .path(DotStatRestQueries.DATA_RESOURCE)
                    .path(ref.getFlowRef().getId())
                    .path(ref.getKey() + "/all")
                    .param("format", "compact_v2");
        }
    }
}
