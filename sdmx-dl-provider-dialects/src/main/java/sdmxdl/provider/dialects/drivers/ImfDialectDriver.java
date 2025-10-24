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

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.io.http.URLQueryBuilder;
import nbbrd.service.ServiceProvider;
import sdmxdl.Feature;
import sdmxdl.Languages;
import sdmxdl.StructureRef;
import sdmxdl.provider.SdmxFix;
import sdmxdl.provider.ri.drivers.RiRestClient;
import sdmxdl.provider.ri.drivers.Sdmx21RestParsers;
import sdmxdl.provider.ri.drivers.Sdmx21RestQueries;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.provider.web.RestClient;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;

import static sdmxdl.Confidentiality.PUBLIC;
import static sdmxdl.provider.SdmxFix.Category.QUERY;
import static sdmxdl.provider.ri.drivers.RiHttpUtils.RI_CONNECTION_PROPERTIES;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class ImfDialectDriver implements Driver {

    private static final String DIALECTS_IMF = "DIALECTS_IMF";

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
                    .confidentiality(PUBLIC)
                    .endpointOf("https://api.imf.org/external/sdmx/2.1")
                    .websiteOf("https://data.imf.org")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/IMF")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/imf")
                    .build())
            .build();

    private static RestClient newClient(WebSource s, Languages languages, WebContext c) throws IOException {
        return RiRestClient.of(s, languages, c, ImfQueries.INSTANCE, Sdmx21RestParsers.DEFAULT, EnumSet.allOf(Feature.class));
    }

    private static final class ImfQueries extends Sdmx21RestQueries {

        public static final ImfQueries INSTANCE = new ImfQueries();

        private ImfQueries() {
            super(false);
        }

        @SdmxFix(id = 1, category = QUERY, cause = "Children reference does not return codelists")
        @Override
        public @NonNull URLQueryBuilder getStructureQuery(@NonNull URL endpoint, @NonNull StructureRef ref) {
            return onMeta(endpoint, DEFAULT_DATASTRUCTURE_PATH, ref)
                    .param(REFERENCES_PARAM, "descendants");
        }
    }
}
