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

import internal.sdmxdl.ri.web.DotStatRestParsers;
import internal.sdmxdl.ri.web.DotStatRestQueries;
import internal.sdmxdl.ri.web.RiHttpUtils;
import internal.sdmxdl.ri.web.RiRestClient;
import internal.util.http.URLQueryBuilder;
import nbbrd.design.VisibleForTesting;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataStructureRef;
import sdmxdl.ext.spi.Dialect;
import sdmxdl.util.DataRef;
import sdmxdl.util.SdmxFix;
import sdmxdl.util.web.RestDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.IOException;
import java.net.URL;

import static sdmxdl.ext.spi.Dialect.SDMX20_DIALECT;
import static sdmxdl.util.SdmxFix.Category.QUERY;

/**
 * @author Philippe Charles
 */
@Deprecated
@ServiceProvider
public final class AbsDriver2 implements WebDriver {

    private static final String RI_ABS = "ri:abs";

    @lombok.experimental.Delegate
    private final RestDriverSupport support = RestDriverSupport
            .builder()
            .name(RI_ABS)
            .rank(NATIVE_RANK)
            .client(AbsDriver2::newClient)
            .supportedProperties(RiHttpUtils.CONNECTION_PROPERTIES)
            .defaultDialect(SDMX20_DIALECT)
            .build();

    private static RiRestClient newClient(SdmxWebSource s, WebContext c) throws IOException {
        return RiRestClient.of(s, c, new AbsQueries(), new DotStatRestParsers(), false);
    }

    @VisibleForTesting
    static final class AbsQueries extends DotStatRestQueries {

        @SdmxFix(id = 1, category = QUERY, cause = "Agency is required in query")
        private static final String AGENCY = "ABS";

        @Override
        public URLQueryBuilder getStructureQuery(URL endpoint, DataStructureRef ref) {
            return super.getStructureQuery(endpoint, ref).path(AGENCY);
        }

        @Override
        public URLQueryBuilder getDataQuery(URL endpoint, DataRef ref) {
            return super.getDataQuery(endpoint, ref).path(AGENCY);
        }
    }
}
