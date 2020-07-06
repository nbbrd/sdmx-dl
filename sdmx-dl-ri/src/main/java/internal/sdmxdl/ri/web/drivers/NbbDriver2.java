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

import internal.sdmxdl.ri.web.DotStatRestClient;
import internal.sdmxdl.ri.web.RestClients;
import internal.util.rest.RestQueryBuilder;
import internal.util.rest.RestClient;
import nbbrd.service.ServiceProvider;
import sdmxdl.LanguagePriorityList;
import sdmxdl.util.SdmxFix;
import sdmxdl.util.web.DataRequest;
import sdmxdl.util.web.SdmxWebClient;
import sdmxdl.util.web.SdmxWebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.IOException;
import java.net.URL;

import static sdmxdl.util.SdmxFix.Category.CONTENT;
import static sdmxdl.util.SdmxFix.Category.QUERY;

/**
 * @author Philippe Charles
 */
@ServiceProvider(SdmxWebDriver.class)
public final class NbbDriver2 implements SdmxWebDriver {

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name("web-ri:nbb")
            .rank(NATIVE_RANK)
            .client(NbbClient2::new)
            .supportedProperties(RestClients.CONNECTION_PROPERTIES)
            .sourceOf("NBB", "National Bank of Belgium", "https://stat.nbb.be/restsdmx/sdmx.ashx")
            .build();

    private static final class NbbClient2 extends DotStatRestClient {

        NbbClient2(SdmxWebSource s, SdmxWebContext c) {
            this(SdmxWebClient.getClientName(s), s.getEndpoint(), c.getLanguages(), RestClients.getRestClient(s, c));
        }

        NbbClient2(String name, URL endpoint, LanguagePriorityList langs, RestClient executor) {
            super(name, endpoint, langs, executor);
        }

        @SdmxFix(id = 1, category = QUERY, cause = "'/all' must be encoded to '%2Fall'")
        @Override
        protected URL getDataQuery(DataRequest request) throws IOException {
            return RestQueryBuilder
                    .of(endpoint)
                    .path(DATA_RESOURCE)
                    .path(request.getFlowRef().getId())
                    .path(request.getKey().toString() + "/all")
                    .param("format", "compact_v2")
                    .build();
        }

        @SdmxFix(id = 2, category = CONTENT, cause = "Some internal errors redirect to an html page")
        @Override
        protected RestClient.Response open(URL query, String mediaType) throws IOException {
            RestClient.Response result = super.open(query, mediaType);
            if (result.getContentType().equals("text/html")) {
                try {
                    throw new IOException("Service not available");
                } finally {
                    result.close();
                }
            }
            return result;
        }
    }
}
