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
package internal.sdmxdl.provider.ri.web.drivers;

import internal.sdmxdl.provider.ri.web.DotStatRestParsers;
import internal.sdmxdl.provider.ri.web.DotStatRestQueries;
import internal.sdmxdl.provider.ri.web.RiHttpUtils;
import internal.sdmxdl.provider.ri.web.RiRestClient;
import internal.util.http.HttpClient;
import internal.util.http.HttpResponse;
import internal.util.http.HttpResponseException;
import internal.util.http.URLQueryBuilder;
import internal.util.http.ext.InterceptingClient;
import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.service.ServiceProvider;
import sdmxdl.LanguagePriorityList;
import sdmxdl.format.MediaType;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.SdmxFix;
import sdmxdl.provider.web.RestDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.IOException;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;
import static sdmxdl.ext.spi.Dialect.SDMX20_DIALECT;
import static sdmxdl.provider.SdmxFix.Category.PROTOCOL;
import static sdmxdl.provider.SdmxFix.Category.QUERY;

/**
 * @author Philippe Charles
 */
@ServiceProvider(WebDriver.class)
public final class NbbDriver2 implements WebDriver {

    private static final String RI_NBB = "ri:nbb";

    @lombok.experimental.Delegate
    private final RestDriverSupport support = RestDriverSupport
            .builder()
            .name(RI_NBB)
            .rank(NATIVE_RANK)
            .client(NbbDriver2::newClient)
            .supportedProperties(RiHttpUtils.CONNECTION_PROPERTIES)
            .defaultDialect(SDMX20_DIALECT)
            .source(SdmxWebSource
                    .builder()
                    .name("NBB")
                    .descriptionOf("National Bank of Belgium")
                    .description("en", "National Bank of Belgium")
                    .description("de", "Belgische National Bank")
                    .description("fr", "Banque Nationale de Belgique")
                    .description("nl", "Nationale Bank van België")
                    .driver(RI_NBB)
                    .endpointOf("https://stat.nbb.be/restsdmx/sdmx.ashx")
                    .websiteOf("https://stat.nbb.be")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/NBB")
                    .build())
            .build();

    private static RiRestClient newClient(SdmxWebSource s, WebContext c) throws IOException {
        return newClient(
                s.getId(),
                s.getEndpoint().toURL(),
                c.getLanguages(),
                RiHttpUtils.newClient(s, c)
        );
    }

    @VisibleForTesting
    static @NonNull RiRestClient newClient(@NonNull String name, @NonNull URL endpoint, @NonNull LanguagePriorityList langs, @NonNull HttpClient executor) {
        return new RiRestClient(name, endpoint, langs, ObsParser::newDefault,
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

    private static final MediaType HTML_TYPE = MediaType.builder().type("text").subtype("html").build();
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
                    .path(ref.getQuery().getKey() + "/all")
                    .param("format", "compact_v2");
        }
    }
}
