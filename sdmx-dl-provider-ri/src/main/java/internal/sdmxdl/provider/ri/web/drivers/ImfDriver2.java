/*
 * Copyright 2017 National Bank of Belgium
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
import internal.sdmxdl.provider.ri.web.RiRestClient;
import internal.util.http.URLQueryBuilder;
import lombok.NonNull;
import nbbrd.io.FileParser;
import nbbrd.io.net.MediaType;
import nbbrd.service.ServiceProvider;
import sdmxdl.*;
import sdmxdl.format.DataCursor;
import sdmxdl.format.ObsParser;
import sdmxdl.format.xml.SdmxXmlStreams;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.SdmxFix;
import sdmxdl.provider.web.RestClient;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.provider.web.WebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static internal.sdmxdl.provider.ri.web.RiHttpUtils.RI_CONNECTION_PROPERTIES;
import static sdmxdl.provider.SdmxFix.Category.QUERY;

/**
 * @author Philippe Charles
 */
@ServiceProvider(WebDriver.class)
public final class ImfDriver2 implements WebDriver {

    private static final String RI_IMF = "ri:imf";

    @lombok.experimental.Delegate
    private final WebDriverSupport support = WebDriverSupport
            .builder()
            .id(RI_IMF)
            .rank(NATIVE_RANK)
            .connector(RestConnector.of(ImfDriver2::newClient))
            .supportedProperties(RI_CONNECTION_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .id("IMF")
                    .name("en", "International Monetary Fund")
                    .driver(RI_IMF)
                    .endpointOf("http://dataservices.imf.org/REST/SDMX_XML.svc")
                    .websiteOf("https://data.imf.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/IMF")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/imf")
                    .build())
            .build();

    private static RestClient newClient(SdmxWebSource s, WebContext c) throws IOException {
        return RiRestClient.of(s, c, new ImfQueries(), new ImfParsers(), IMF_FEATURES);
    }

    @SdmxFix(id = 1, category = QUERY, cause = "Data detail parameter not supported")
    private static final Set<Feature> IMF_FEATURES = EnumSet.of(Feature.DATA_QUERY_ALL_KEYWORD);

    private static final class ImfQueries extends DotStatRestQueries {

        @Override
        public URLQueryBuilder getFlowsQuery(URL endpoint) {
            return URLQueryBuilder
                    .of(endpoint)
                    .path("Dataflow");
        }

        @Override
        public URLQueryBuilder getStructureQuery(URL endpoint, DataStructureRef ref) {
            return URLQueryBuilder
                    .of(endpoint)
                    .path("DataStructure")
                    .path(ref.getId());
        }

        @Override
        public URLQueryBuilder getDataQuery(URL endpoint, DataRef ref, @NonNull DataStructureRef dsdRef) {
            return URLQueryBuilder
                    .of(endpoint)
                    .path("CompactData")
                    .path(dsdRef.getId())
                    .path(ref.getQuery().getKey().toString());
        }
    }

    private static final class ImfParsers extends DotStatRestParsers {

        @Override
        public @NonNull FileParser<List<Dataflow>> getFlowsParser(@NonNull MediaType mediaType, @NonNull LanguagePriorityList langs) {
            return SdmxXmlStreams.flow20(langs);
        }

        @Override
        public @NonNull FileParser<DataCursor> getDataParser(@NonNull MediaType mediaType, @NonNull DataStructure dsd, @NonNull Supplier<ObsParser> dataFactory) {
            return SdmxXmlStreams.compactData20(dsd, dataFactory);
        }
    }
}
