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

import internal.sdmxdl.provider.ri.web.RiHttpUtils;
import internal.sdmxdl.provider.ri.web.Sdmx21RestQueries;
import internal.sdmxdl.provider.ri.web.RiRestClient;
import internal.sdmxdl.provider.ri.web.Sdmx21RestParsers;
import internal.util.http.URLQueryBuilder;
import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.service.ServiceProvider;
import sdmxdl.*;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.SdmxFix;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.web.RestDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.IOException;
import java.net.URL;
import java.util.stream.Stream;

import static sdmxdl.ext.spi.Dialect.SDMX20_DIALECT;
import static sdmxdl.provider.SdmxFix.Category.QUERY;

/**
 * @author Philippe Charles
 */
@ServiceProvider(WebDriver.class)
public final class BbkDriver implements WebDriver {

    private static final String RI_BBK = "ri:bbk";

    @lombok.experimental.Delegate
    private final RestDriverSupport support = RestDriverSupport
            .builder()
            .name(RI_BBK)
            .rank(NATIVE_RANK)
            .client(BbkClient::new)
            .supportedProperties(RiHttpUtils.CONNECTION_PROPERTIES)
            .defaultDialect(DIALECT)
            .source(SdmxWebSource
                    .builder()
                    .name("BBK")
                    .descriptionOf("Deutsche Bundesbank")
                    .driver(RI_BBK)
                    .endpointOf("https://api.statistiken.bundesbank.de/rest")
                    .websiteOf("https://www.bundesbank.de/en/statistics/time-series-databases")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/BBK")
                    .build())
            .build();

    @VisibleForTesting
    static final class BbkClient extends RiRestClient {

        BbkClient(SdmxWebSource s, WebContext c) throws IOException {
            super(
                    s.getId(),
                    s.getEndpoint().toURL(),
                    c.getLanguages(),
                    ObsParser::newDefault,
                    RiHttpUtils.newClient(s, c),
                    new BbkQueries(),
                    new Sdmx21RestParsers(),
                    true);
        }

        @Override
        public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull DataStructure dsd) throws IOException {
            return super.getData(fixDataRef(ref, dsd), dsd);
        }

        private DataRef fixDataRef(DataRef ref, DataStructure dsd) {
            return ref.getQuery().getKey().equals(Key.ALL)
                    ? DataRef.of(ref.getFlowRef(), DataQuery.of(alternateAllOf(dsd), ref.getQuery().getDetail()))
                    : ref;
        }

        @SdmxFix(id = 6, category = QUERY, cause = "Data key parameter does not support 'all' keyword")
        private Key alternateAllOf(DataStructure dsd) {
            return Key.of(new String[dsd.getDimensions().size()]);
        }
    }

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
        protected URLQueryBuilder onData(URL endpoint, String resourcePath, DataflowRef flowRef, Key key, String providerRef) {
            return URLQueryBuilder
                    .of(endpoint)
                    .path(resourcePath)
                    .path(flowRef.getId())
                    .path(key.toString());
        }

        @SdmxFix(id = 5, category = QUERY, cause = "Data detail parameter for series-keys-only has a typo")
        @Override
        protected void applyFilter(DataDetail detail, URLQueryBuilder result) {
            if (detail.equals(DataDetail.SERIES_KEYS_ONLY)) {
                result.param(DETAIL_PARAM, "serieskeyonly");
            } else {
                super.applyFilter(detail, result);
            }
        }
    }

    // FIXME: use TIME_FORMAT attribute instead of FREQ dimension in SDMX21 ?
    private static final String DIALECT = SDMX20_DIALECT;
}
