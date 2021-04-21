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
package internal.sdmxdl.connectors.drivers;

import internal.sdmxdl.connectors.ConnectorRestClient;
import internal.sdmxdl.connectors.HasDetailSupported;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.client.custom.ILO_Legacy;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.parser.v20.DataflowParser;
import it.bancaditalia.oss.sdmx.parser.v21.Sdmx21Queries;
import it.bancaditalia.oss.sdmx.util.RestQueryBuilder;
import nbbrd.service.ServiceProvider;
import sdmxdl.util.web.SdmxWebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebDriver;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@ServiceProvider(SdmxWebDriver.class)
public final class IloDriver implements SdmxWebDriver {

    private static final String CONNECTORS_ILO = "connectors:ilo";

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name(CONNECTORS_ILO)
            .rank(WRAPPED_RANK)
            .client(ConnectorRestClient.of(ILO2::new, "SDMX20"))
            .supportedProperties(ConnectorRestClient.CONNECTION_PROPERTIES)
            .source(SdmxWebSource
                    .builder()
                    .name("ILO")
                    .description("International Labour Office")
                    .driver(CONNECTORS_ILO)
                    .endpointOf("https://www.ilo.org/ilostat/sdmx/ws/rest")
                    .websiteOf("https://ilostat.ilo.org/data/")
                    .build())
            .build();

    private static final class ILO2 extends ILO_Legacy implements HasDetailSupported {

        public ILO2() throws URISyntaxException {
        }

        @Override
        public boolean isDetailSupported() {
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
            return runQuery(new DataflowParser(), query, null, null)
                    .stream()
                    .collect(Collectors.toMap(Dataflow::getId, Function.identity()));
        }

        @Override
        protected URL buildDataQuery(Dataflow dataflow, String resource, String startTime, String endTime, boolean serieskeysonly, String updatedAfter, boolean includeHistory) {
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
