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
import nbbrd.design.VisibleForTesting;
import nbbrd.io.http.URLQueryBuilder;
import nbbrd.service.ServiceProvider;
import sdmxdl.*;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.HasMarker;
import sdmxdl.provider.SdmxFix;
import sdmxdl.provider.ri.drivers.*;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;

import static sdmxdl.Confidentiality.PUBLIC;
import static sdmxdl.provider.SdmxFix.Category.QUERY;
import static sdmxdl.provider.ri.drivers.RiHttpUtils.RI_CONNECTION_PROPERTIES;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class BbkDialectDriver implements Driver {

    private static final String DIALECTS_BBK = "DIALECTS_BBK";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(DIALECTS_BBK)
            .rank(NATIVE_DRIVER_RANK)
            .connector(RestConnector.of(BbkDialectDriver::newClient))
            .properties(RI_CONNECTION_PROPERTIES)
            .source(WebSource
                    .builder()
                    .id("BBK")
                    .name("en", "Deutsche Bundesbank")
                    .name("de", "Deutsche Bundesbank")
                    .driver(DIALECTS_BBK)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://api.statistiken.bundesbank.de/rest")
                    .websiteOf("https://www.bundesbank.de/en/statistics/time-series-databases")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/BBK")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/bbk")
                    .build())
            .build();

    private static RiRestClient newClient(WebSource s, Languages languages, WebContext c) throws IOException {
        return new RiRestClient(
                HasMarker.of(s),
                s.getEndpoint().toURL(),
                languages,
                ObsParser::newDefault,
                RiHttpUtils.newClient(s, c),
                new BbkQueries(),
                new Sdmx21RestParsers(),
                Sdmx21RestErrors.DEFAULT,
                BBK_FEATURES
        );
    }

    @SdmxFix(id = 6, category = QUERY, cause = "Data key parameter does not support 'all' keyword")
    private static final Set<Feature> BBK_FEATURES = EnumSet.of(Feature.DATA_QUERY_DETAIL);

    @VisibleForTesting
    static final class BbkQueries extends Sdmx21RestQueries {

        BbkQueries() {
            super(false);
        }

        @SdmxFix(id = 2, category = QUERY, cause = "Resource ref does not support 'all' in agencyID")
        private static final String AGENCY_ID = "BBK";

        @SdmxFix(id = 3, category = QUERY, cause = "Resource ref does not support 'all' in resourceID")
        private static boolean isValid(ResourceRef<?> ref) {
            return !ref.getId().equals("all");
        }

        @SdmxFix(id = 1, category = QUERY, cause = "Meta uses custom resources path")
        @Override
        protected URLQueryBuilder onMeta(URL endpoint, String resourcePath, ResourceRef<?> ref) {
            URLQueryBuilder result = URLQueryBuilder
                    .of(endpoint)
                    .path("metadata")
                    .path(resourcePath)
                    .path(AGENCY_ID);
            if (isValid(ref)) {
                result.path(ref.getId());
            }
            return result;
        }

        @SdmxFix(id = 4, category = QUERY, cause = "Data does not support providerRef")
        @Override
        protected URLQueryBuilder onData(URL endpoint, String resourcePath, FlowRef flowRef, Key key, String providerRef) {
            return URLQueryBuilder
                    .of(endpoint)
                    .path(resourcePath)
                    .path(flowRef.getId())
                    .path(key.toString());
        }

        @SdmxFix(id = 5, category = QUERY, cause = "Data detail parameter for series-keys-only has a typo")
        @Override
        protected void applyFilter(Detail detail, URLQueryBuilder result) {
            if (detail.equals(Detail.SERIES_KEYS_ONLY)) {
                result.param(DETAIL_PARAM, "serieskeyonly");
            } else {
                super.applyFilter(detail, result);
            }
        }
    }
}
