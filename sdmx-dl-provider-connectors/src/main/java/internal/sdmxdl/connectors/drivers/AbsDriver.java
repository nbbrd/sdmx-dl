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
import it.bancaditalia.oss.sdmx.client.custom.ABS;
import nbbrd.service.ServiceProvider;
import sdmxdl.util.parser.DefaultObsParser;
import sdmxdl.util.web.RestDriverSupport;
import sdmxdl.web.spi.WebDriver;

import static sdmxdl.ext.spi.Dialect.SDMX20_DIALECT;

/**
 * @author Philippe Charles
 */
@Deprecated
@ServiceProvider(WebDriver.class)
public final class AbsDriver implements WebDriver {

    private static final String CONNECTORS_ABS = "connectors:abs";

    @lombok.experimental.Delegate
    private final RestDriverSupport support = RestDriverSupport
            .builder()
            .name(CONNECTORS_ABS)
            .rank(WRAPPED_RANK)
            .client(ConnectorRestClient.of(ABS::new, DefaultObsParser::newDefault))
            .supportedProperties(ConnectorRestClient.CONNECTION_PROPERTIES)
            .defaultDialect(SDMX20_DIALECT)
            .build();
}
