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
package internal.sdmxdl.ri.web.drivers;

import internal.sdmxdl.ri.web.RiHttpUtils;
import internal.sdmxdl.ri.web.RiRestClient;
import internal.sdmxdl.ri.web.Sdmx21RestParsers;
import internal.sdmxdl.ri.web.Sdmx21RestQueries;
import internal.util.http.HttpClient;
import internal.util.http.URLQueryBuilder;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.ObsParser;
import sdmxdl.util.SdmxFix;
import sdmxdl.util.parser.DefaultObsParser;
import sdmxdl.util.parser.FreqFactory;
import sdmxdl.util.parser.PeriodParsers;
import sdmxdl.util.web.DataRequest;
import sdmxdl.util.web.SdmxWebClient;
import sdmxdl.util.web.SdmxWebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import static sdmxdl.util.SdmxFix.Category.QUERY;

/**
 * @author Philippe Charles
 */
@ServiceProvider(SdmxWebDriver.class)
public final class BbkDriver implements SdmxWebDriver {

    private static final String RI_BBK = "ri:bbk";

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name(RI_BBK)
            .rank(NATIVE_RANK)
            .client(BbkDriver::newClient)
            .supportedProperties(RiHttpUtils.CONNECTION_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .name("BBK")
                    .description("Bundesbank")
                    .driver(RI_BBK)
                    .endpointOf("https://api.statistiken.bundesbank.de/rest")
                    .websiteOf("https://www.bundesbank.de/en/statistics/time-series-databases")
                    .monitorOf("Upptime", "nbbrd:sdmx-upptime:BBK")
                    .build())
            .build();

    private static @NonNull SdmxWebClient newClient(@NonNull SdmxWebSource s, @NonNull SdmxWebContext c) {
        return new BbkClient(
                SdmxWebClient.getClientName(s),
                s.getEndpoint(),
                c.getLanguages(),
                RiHttpUtils.newClient(RiHttpUtils.newContext(s, c))
        );
    }

    @VisibleForTesting
    static final class BbkClient extends RiRestClient {

        private static final BbkObsFactory OBS_FACTORY = new BbkObsFactory();
        private static final BbkQueries QUERIES = new BbkQueries();
        private static final Sdmx21RestParsers PARSERS = new Sdmx21RestParsers();

        public BbkClient(String name, URL endpoint, LanguagePriorityList langs, HttpClient executor) {
            super(name, endpoint, langs, OBS_FACTORY, executor, QUERIES, PARSERS, true);
        }

        @Override
        public @NonNull DataCursor getData(@NonNull DataRequest request, @NonNull DataStructure dsd) throws IOException {
            if (request.getKey().equals(Key.ALL)) {
                request = request.toBuilder().key(alternateAllOf(dsd)).build();
            }
            return super.getData(request, dsd);
        }

        @SdmxFix(id = 6, category = QUERY, cause = "Data key parameter does not support 'all' keyword")
        private Key alternateAllOf(DataStructure dsd) {
            return Key.of(new String[dsd.getDimensions().size()]);
        }
    }

    @VisibleForTesting
    static final class BbkQueries extends Sdmx21RestQueries {

        public BbkQueries() {
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
        protected void applyFilter(DataFilter filter, URLQueryBuilder result) {
            if (filter.getDetail().equals(DataFilter.Detail.SERIES_KEYS_ONLY)) {
                result.param(DETAIL_PARAM, "serieskeyonly");
            } else {
                super.applyFilter(filter, result);
            }
        }
    }

    // FIXME: use TIME_FORMAT attribute instead of FREQ dimension in SDMX21 ?
    private static final class BbkObsFactory implements ObsFactory {

        @Override
        public @NonNull ObsParser getObsParser(@NonNull DataStructure dsd) {
            Objects.requireNonNull(dsd);
            return new DefaultObsParser(
                    FreqFactory.sdmx20(dsd),
                    PeriodParsers::onStandardFreq,
                    Parser.onDouble()
            );
        }
    }
}
