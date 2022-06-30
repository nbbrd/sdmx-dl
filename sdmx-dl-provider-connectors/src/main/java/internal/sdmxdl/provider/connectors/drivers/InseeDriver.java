/*
 * Copyright 2016 National Bank of Belgium
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
package internal.sdmxdl.provider.connectors.drivers;

import internal.sdmxdl.provider.connectors.ConnectorsRestClient;
import internal.sdmxdl.provider.connectors.Connectors;
import internal.sdmxdl.provider.connectors.HasDetailSupported;
import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import sdmxdl.provider.SdmxFix;
import sdmxdl.format.ObsParser;
import sdmxdl.format.time.StandardReportingFormat;
import sdmxdl.format.time.ObsTimeParser;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.provider.web.WebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebDriver;

import java.net.URI;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;

import static internal.sdmxdl.provider.connectors.ConnectorsRestClient.CONNECTORS_CONNECTION_PROPERTIES;
import static sdmxdl.provider.SdmxFix.Category.CONTENT;

/**
 * @author Philippe Charles
 */
@ServiceProvider
public final class InseeDriver implements WebDriver {

    private static final String CONNECTORS_INSEE = "connectors:insee";

    @lombok.experimental.Delegate
    private final WebDriverSupport support = WebDriverSupport
            .builder()
            .name(CONNECTORS_INSEE)
            .rank(WRAPPED_RANK)
            .connector(RestConnector.of(ConnectorsRestClient.ofGeneric(InseeClient::new, OBS_FACTORY)))
            .supportedProperties(CONNECTORS_CONNECTION_PROPERTIES)
            .defaultDialect(INSEE_2017)
            .source(SdmxWebSource
                    .builder()
                    .name("INSEE")
                    .descriptionOf("Institut national de la statistique et des études économiques")
                    .driver(CONNECTORS_INSEE)
                    .endpointOf("https://bdm.insee.fr/series/sdmx")
                    .websiteOf("https://www.insee.fr/fr/statistiques")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/INSEE")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/insee")
                    .build())
            .build();

    @SdmxFix(id = 2, category = CONTENT, cause = "Does not follow sdmx standard codes")
    private static final String INSEE_2017 = "INSEE2017";

    private final static class InseeClient extends RestSdmxClient implements HasDetailSupported {

        private InseeClient(URI endpoint, Map<?, ?> properties) {
            super("", endpoint, false, false, true);
        }

        @Override
        public DataFlowStructure getDataFlowStructure(DSDIdentifier dsd, boolean full) throws SdmxException {
            DataFlowStructure result = super.getDataFlowStructure(dsd, full);
            fixIds(result);
            fixMissingCodes(result);
            return result;
        }

        @Override
        public boolean isDetailSupported() {
            return true;
        }

        @SdmxFix(id = 3, category = CONTENT, cause = "Some dimension/code ids are invalid")
        private void fixIds(DataFlowStructure dsd) {
            for (Dimension d : dsd.getDimensions()) {
                if (d.getId().endsWith("6")) {
                    d.setId(getValidId(d.getId()));
//                    d.getCodeList().setId(getValidId(d.getCodeList().getId()));
                }
            }
        }

        private String getValidId(String id) {
            return id.substring(0, id.length() - 1);
        }

        @SdmxFix(id = 4, category = CONTENT, cause = "Some codes are missing in dsd even when requested with 'references=children'")
        private void fixMissingCodes(DataFlowStructure dsd) throws SdmxException {
            for (Dimension d : dsd.getDimensions()) {
                Codelist codelist = d.getCodeList();
                if (codelist.isEmpty()) {
                    loadMissingCodes(codelist);
                }
            }
        }

        private void loadMissingCodes(Codelist codelist) throws SdmxException {
            try {
                codelist.setCodes(super.getCodes(codelist.getId(), codelist.getAgency(), codelist.getVersion()));
            } catch (SdmxException ex) {
                if (!Connectors.isNoResultMatchingQuery(ex)) {
                    throw ex;
                }
                logger.log(Level.WARNING, "Cannot retrieve codes for ''{0}''", codelist.getFullIdentifier());
            }
        }
    }

    private static final StandardReportingFormat REPORTING_TWO_MONTH = StandardReportingFormat
            .builder()
            .indicator('B')
            .durationOf("P2M")
            .limitPerYear(6)
            .build();

    private static final ObsTimeParser EXTENDED_TIME_PARSER =
            ObsTimeParser.onObservationalTimePeriod()
                    .orElse(ObsTimeParser.onStandardReporting(REPORTING_TWO_MONTH));

    private static final Supplier<ObsParser> OBS_FACTORY = () -> new ObsParser(EXTENDED_TIME_PARSER, Parser.onDouble());
}
