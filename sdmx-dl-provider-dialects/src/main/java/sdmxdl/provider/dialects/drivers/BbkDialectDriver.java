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
import nbbrd.design.VisibleForTesting;
import nbbrd.io.http.UriQueryBuilder;
import nbbrd.io.text.BaseProperty;
import nbbrd.service.ServiceProvider;
import sdmxdl.*;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.HasMarker;
import sdmxdl.provider.SdmxFix;
import sdmxdl.provider.ri.drivers.RiRestClient;
import sdmxdl.provider.ri.drivers.Sdmx21RestErrors;
import sdmxdl.provider.ri.drivers.Sdmx21RestParsers;
import sdmxdl.provider.ri.drivers.Sdmx21RestQueries;
import sdmxdl.provider.ri.http.HttpFactory;
import sdmxdl.provider.ri.http.HttpManager;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.provider.web.RestClient;
import sdmxdl.provider.web.RestClientFactory;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static sdmxdl.Confidentiality.PUBLIC;
import static sdmxdl.provider.SdmxFix.Category.QUERY;

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
            .connectorOf(new BbkRestClientFactory())
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

    private static final class BbkRestClientFactory implements RestClientFactory {

        private final HttpFactory httpFactory = HttpManager.getHttpFactory();

        @Override
        public @NonNull List<BaseProperty> getRestClientProperties() {
            return httpFactory.getHttpClientProperties();
        }

        @Override
        public @NonNull RestClient createRestClient(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) {
            return new RiRestClient(
                    HasMarker.of(source),
                    source.getEndpoint(),
                    languages,
                    ObsParser::newDefault,
                    httpFactory.createHttpClient(source, context),
                    BbkQueries.INSTANCE,
                    Sdmx21RestParsers.DEFAULT,
                    Sdmx21RestErrors.DEFAULT,
                    BBK_FEATURES
            );
        }
    }

    @SdmxFix(id = 6, category = QUERY, cause = "Data key parameter does not support 'all' keyword")
    private static final Set<Feature> BBK_FEATURES = EnumSet.of(Feature.DATA_QUERY_DETAIL);

    @VisibleForTesting
    static final class BbkQueries extends Sdmx21RestQueries {

        public static final BbkQueries INSTANCE = new BbkQueries();

        private BbkQueries() {
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
        protected UriQueryBuilder onMeta(URI endpoint, String resourcePath, ResourceRef<?> ref) {
            UriQueryBuilder result = UriQueryBuilder
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
        protected UriQueryBuilder onData(URI endpoint, String resourcePath, FlowRef flowRef, Key key, String providerRef) {
            return UriQueryBuilder
                    .of(endpoint)
                    .path(resourcePath)
                    .path(flowRef.getId())
                    .path(key.toString());
        }

        @SdmxFix(id = 5, category = QUERY, cause = "Data detail parameter for series-keys-only has a typo")
        @Override
        protected void applyFilter(Detail detail, UriQueryBuilder result) {
            if (detail.equals(Detail.SERIES_KEYS_ONLY)) {
                result.param(DETAIL_PARAM, "serieskeyonly");
            } else {
                super.applyFilter(detail, result);
            }
        }
    }
}
