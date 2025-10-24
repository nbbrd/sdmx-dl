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
package sdmxdl.provider.connectors.drivers;

import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.SDMXReference;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import nbbrd.design.DirectImpl;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import sdmxdl.Duration;
import sdmxdl.format.ObsParser;
import sdmxdl.format.time.ObservationalTimePeriod;
import sdmxdl.format.time.StandardReportingFormat;
import sdmxdl.format.time.TimeFormats;
import sdmxdl.provider.SdmxFix;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.provider.web.RestConnector;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;

import java.net.URI;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;

import static sdmxdl.Confidentiality.PUBLIC;
import static sdmxdl.format.time.TimeFormats.IGNORE_ERROR;
import static sdmxdl.provider.SdmxFix.Category.CONTENT;
import static sdmxdl.provider.connectors.drivers.ConnectorsRestClient.CONNECTORS_CONNECTION_PROPERTIES;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class InseeDriver implements Driver {

    private static final String CONNECTORS_INSEE = "CONNECTORS_INSEE";

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(CONNECTORS_INSEE)
            .rank(WRAPPED_DRIVER_RANK)
            .connector(RestConnector.of(ConnectorsRestClient.ofGeneric(InseeClient::new, OBS_FACTORY)))
            .properties(CONNECTORS_CONNECTION_PROPERTIES)
            .source(WebSource
                    .builder()
                    .id("INSEE")
                    .name("fr", "Institut national de la statistique et des études économiques")
                    .driver(CONNECTORS_INSEE)
                    .confidentiality(PUBLIC)
                    .endpointOf("https://bdm.insee.fr/series/sdmx")
                    .websiteOf("https://www.insee.fr/fr/statistiques")
                    .monitorOf("upptime:/nbbrd/sdmx-upptime/INSEE")
                    .monitorWebsiteOf("https://nbbrd.github.io/sdmx-upptime/history/insee")
                    .build())
            .build();

    private final static class InseeClient extends RestSdmxClient implements HasDetailSupported {

        private InseeClient(URI endpoint, Map<?, ?> properties) {
            super("", endpoint, false, false, true);
        }

        @Override
        public DataFlowStructure getDataFlowStructure(SDMXReference dsd, boolean full) throws SdmxException {
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
//                    d.setId(getValidId(d.getId()));
                }
            }
        }

        private String getValidId(String id) {
            return id.substring(0, id.length() - 1);
        }

        @SdmxFix(id = 4, category = CONTENT, cause = "Some codes are missing in dsd even when requested with 'references=children'")
        private void fixMissingCodes(DataFlowStructure dsd) throws SdmxException {
            for (Dimension d : dsd.getDimensions()) {
                SDMXReference codelist = d.getCodeList();
//                if (codelist.isEmpty()) {
//                    loadMissingCodes(codelist);
//                }
            }
        }

        private void loadMissingCodes(Codelist codelist) throws SdmxException {
//            try {
//                codelist.setCodes(super.getCodes(codelist.getId(), codelist.getAgency(), codelist.getVersion()));
//            } catch (SdmxException ex) {
//                if (!Connectors.isNoResultMatchingQuery(ex)) {
//                    throw ex;
//                }
            LOGGER.log(Level.WARNING, "Cannot retrieve codes for ''{0}''", codelist.getFullIdentifier());
//            }
        }
    }

    private static final StandardReportingFormat REPORTING_TWO_MONTH = StandardReportingFormat
            .builder()
            .indicator('B')
            .duration(Duration.parse("P2M"))
            .limitPerYear(6)
            .build();

    private static final Parser<ObservationalTimePeriod> EXTENDED_TIME_PARSER =
            TimeFormats.getObservationalTimePeriod(IGNORE_ERROR)
                    .orElse(TimeFormats.onReportingFormat(REPORTING_TWO_MONTH, IGNORE_ERROR));

    private static final Supplier<ObsParser> OBS_FACTORY = () -> new ObsParser(EXTENDED_TIME_PARSER, Parser.onDouble());
}
