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
package sdmxdl.provider.dialects.drivers;

import nbbrd.design.DirectImpl;
import sdmxdl.provider.ri.drivers.RiRestClient;
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
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.provider.web.RestClient;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static sdmxdl.provider.ri.drivers.RiHttpUtils.RI_CONNECTION_PROPERTIES;
import static sdmxdl.provider.SdmxFix.Category.QUERY;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class ImfDialectDriver implements Driver {

    private static final String DIALECTS_IMF = "dialects:imf";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(DIALECTS_IMF)
            .rank(NATIVE_DRIVER_RANK)
            .connector(RestConnector.of(ImfDialectDriver::newClient))
            .properties(RI_CONNECTION_PROPERTIES)
            .source(WebSource
                    .builder()
                    .id("IMF")
                    .name("en", "International Monetary Fund")
                    .driver(DIALECTS_IMF)
                    .endpointOf("http://dataservices.imf.org/REST/SDMX_XML.svc")
                    .websiteOf("https://data.imf.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/IMF")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/imf")
                    .build())
            .build();

    private static RestClient newClient(WebSource s, Languages languages, WebContext c) throws IOException {
        return RiRestClient.of(s, languages, c, new ImfQueries(), new ImfParsers(), IMF_FEATURES);
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
        public URLQueryBuilder getStructureQuery(URL endpoint, StructureRef ref) {
            return URLQueryBuilder
                    .of(endpoint)
                    .path("DataStructure")
                    .path(ref.getId());
        }

        @Override
        public URLQueryBuilder getDataQuery(URL endpoint, DataRef ref, @NonNull StructureRef dsdRef) {
            return URLQueryBuilder
                    .of(endpoint)
                    .path("CompactData")
                    .path(dsdRef.getId())
                    .path(ref.getQuery().getKey().toString());
        }
    }

    private static final class ImfParsers extends DotStatRestParsers {

        @Override
        public @NonNull FileParser<List<Flow>> getFlowsParser(@NonNull MediaType mediaType, @NonNull Languages langs) {
            return SdmxXmlStreams.flow20(langs);
        }

        @Override
        public @NonNull FileParser<DataCursor> getDataParser(@NonNull MediaType mediaType, @NonNull Structure dsd, @NonNull Supplier<ObsParser> dataFactory) {
            return SdmxXmlStreams.compactData20(dsd, dataFactory);
        }
    }
}
