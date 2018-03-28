/*
 * Copyright 2015 National Bank of Belgium
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
package internal.connectors.drivers;

import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.util.SdmxFix;
import it.bancaditalia.oss.sdmx.client.custom.ILO;
import org.openide.util.lookup.ServiceProvider;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.connectors.ConnectorRestClient;
import internal.connectors.HasSeriesKeysOnlySupported;
import internal.web.SdmxWebDriverSupport;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.parser.v20.DataflowParser;
import it.bancaditalia.oss.sdmx.parser.v21.Sdmx21Queries;
import it.bancaditalia.oss.sdmx.util.RestQueryBuilder;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxWebDriver.class)
public final class IloDriver implements SdmxWebDriver, HasCache {

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name("ilo@connectors")
            .client(ConnectorRestClient.of(ILO2::new))
            .supportedProperties(ConnectorRestClient.CONNECTION_PROPERTIES)
            .sourceOf("ILO", "International Labour Office", FALLBACK_URL)
            .build();

    @SdmxFix(id = "ILO#1", cause = "Fallback to http due to servers redirecting to http")
    private static final String FALLBACK_URL = "http://www.ilo.org/ilostat/sdmx/ws/rest";

    private static final class ILO2 extends ILO implements HasSeriesKeysOnlySupported {

        public ILO2() throws URISyntaxException {
        }

        @Override
        public boolean isSeriesKeysOnlySupported() {
            return true;
        }

        @Override
        public Map<String, Dataflow> getDataflows() throws SdmxException {
            URL query;
            try {
                query = new RestQueryBuilder(endpoint).addPath("dataflow").addPath("ILO").build();
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
            return runQuery(new DataflowParser(), query, null)
                    .stream()
                    .collect(Collectors.toMap(Dataflow::getId, Function.identity()));
        }

        @Override
        protected URL buildDataQuery(Dataflow dataflow, String resource, String startTime, String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) throws SdmxException {
            try {
                return new Sdmx21Queries(endpoint)
                        .addParams(startTime, endTime, serieskeysonly, updatedAfter, includeHistory, format)
                        .addPath("data")
                        .addPath(dataflow.getFullIdentifier())
                        .addPath("all".equals(resource) ? "ALL" : resource)
                        .build();
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
