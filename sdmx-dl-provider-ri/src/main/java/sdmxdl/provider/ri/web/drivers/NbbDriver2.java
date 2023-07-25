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
package sdmxdl.provider.ri.web.drivers;

import internal.util.http.HttpClient;
import internal.util.http.HttpResponse;
import internal.util.http.HttpResponseException;
import internal.util.http.URLQueryBuilder;
import internal.util.http.ext.InterceptingClient;
import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.net.MediaType;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataStructureRef;
import sdmxdl.Feature;
import sdmxdl.Languages;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.HasMarker;
import sdmxdl.provider.Marker;
import sdmxdl.provider.SdmxFix;
import sdmxdl.provider.ri.web.*;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;

import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;
import static sdmxdl.provider.SdmxFix.Category.PROTOCOL;
import static sdmxdl.provider.SdmxFix.Category.QUERY;
import static sdmxdl.provider.ri.web.RiHttpUtils.RI_CONNECTION_PROPERTIES;

/**
 * @author Philippe Charles
 */
@ServiceProvider(Driver.class)
public final class NbbDriver2 implements Driver {

    private static final String RI_NBB = "ri:nbb";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(RI_NBB)
            .rank(NATIVE_DRIVER_RANK)
            .connector(RestConnector.of(NbbDriver2::newClient))
            .properties(RI_CONNECTION_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .id("NBB")
                    .name("en", "National Bank of Belgium")
                    .name("de", "Belgische National Bank")
                    .name("fr", "Banque Nationale de Belgique")
                    .name("nl", "Nationale Bank van België")
                    .driver(RI_NBB)
                    .endpointOf("https://stat.nbb.be/restsdmx/sdmx.ashx")
                    .websiteOf("https://stat.nbb.be")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/NBB")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/nbb")
                    .build())
            .build();

    private static RiRestClient newClient(SdmxWebSource s, Languages languages, WebContext c) throws IOException {
        return newClient(
                HasMarker.of(s),
                s.getEndpoint().toURL(),
                languages,
                RiHttpUtils.newClient(s, c)
        );
    }

    @VisibleForTesting
    static @NonNull RiRestClient newClient(@NonNull Marker marker, @NonNull URL endpoint, @NonNull Languages langs, @NonNull HttpClient executor) {
        return new RiRestClient(marker, endpoint, langs, ObsParser::newDefault,
                new InterceptingClient(executor, (client, request, response) -> checkInternalErrorRedirect(response)),
                new NbbQueries(),
                new DotStatRestParsers(),
                Sdmx21RestErrors.DEFAULT,
                EnumSet.of(Feature.DATA_QUERY_ALL_KEYWORD));
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
        public URLQueryBuilder getDataQuery(URL endpoint, DataRef ref, @NonNull DataStructureRef dsdRef) {
            return URLQueryBuilder
                    .of(endpoint)
                    .path(DotStatRestQueries.DATA_RESOURCE)
                    .path(ref.getFlowRef().getId())
                    .path(ref.getQuery().getKey() + "/all")
                    .param("format", "compact_v2");
        }
    }
}
