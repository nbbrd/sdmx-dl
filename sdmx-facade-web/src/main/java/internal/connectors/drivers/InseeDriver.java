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
package internal.connectors.drivers;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.Series;
import be.nbb.sdmx.facade.parser.spi.SdmxDialect;
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.util.SdmxFix;
import be.nbb.sdmx.facade.util.SdmxMediaType;
import be.nbb.sdmx.facade.util.SeriesSupport;
import be.nbb.sdmx.facade.xml.stream.SdmxXmlStreams;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxIOException;
import java.io.IOException;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.connectors.ConnectorRestClient;
import internal.connectors.HasDataCursor;
import internal.connectors.HasSeriesKeysOnlySupported;
import internal.connectors.Connectors;
import internal.org.springframework.util.xml.XMLEventStreamReader;
import internal.util.drivers.InseeDialect;
import internal.web.SdmxWebDriverSupport;
import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.client.Parser;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Philippe Charles
 */
@lombok.extern.java.Log
@ServiceProvider(service = SdmxWebDriver.class)
public final class InseeDriver implements SdmxWebDriver, HasCache {

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name("insee@connectors")
            .client(ConnectorRestClient.of(InseeClient::new))
            .supportedProperties(ConnectorRestClient.CONNECTION_PROPERTIES)
            .sourceOf("INSEE", "Institut national de la statistique et des études économiques", FALLBACK_URL)
            .build();

    @SdmxFix(id = "INSEE#1", cause = "Fallback to http due to some servers that use root certificate unknown to jdk'")
    private static final String FALLBACK_URL = "http://bdm.insee.fr/series/sdmx";

    private final static class InseeClient extends RestSdmxClient implements HasDataCursor, HasSeriesKeysOnlySupported {

        @SdmxFix(id = "INSEE#2", cause = "Does not follow sdmx standard codes")
        private final SdmxDialect dialect;

        private InseeClient(URI endpoint, Map<?, ?> properties) {
            super("", endpoint, false, false, true);
            this.dialect = new InseeDialect();
        }

        @Override
        public DataFlowStructure getDataFlowStructure(DSDIdentifier dsd, boolean full) throws SdmxException {
            DataFlowStructure result = super.getDataFlowStructure(dsd, full);
            fixIds(result);
            fixMissingCodes(result);
            return result;
        }

        @Override
        public DataCursor getDataCursor(DataflowRef flowRef, DataStructure dsd, Key resource, boolean serieskeysonly) throws SdmxException, IOException {
            // FIXME: avoid in-memory copy
            return SeriesSupport.asCursor(getData(flowRef, dsd, resource, serieskeysonly), resource);
        }

        @Override
        public boolean isSeriesKeysOnlySupported() {
            return true;
        }

        @SdmxFix(id = "INSEE#4", cause = "Some dimension/code ids are invalid")
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

        @SdmxFix(id = "INSEE#3", cause = "Some codes are missing in dsd even when requested with 'references=children'")
        private void fixMissingCodes(DataFlowStructure dsd) throws SdmxException {
            for (Dimension d : dsd.getDimensions()) {
                Codelist codelist = d.getCodeList();
                if (codelist.getCodes().isEmpty()) {
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
                log.log(Level.WARNING, "Cannot retrieve codes for ''{0}''", codelist.getFullIdentifier());
            }
        }

        private List<Series> getData(DataflowRef flowRef, DataStructure dsd, Key resource, boolean serieskeysonly) throws SdmxException {
            return runQuery(getCompactData21Parser(dsd),
                    buildDataQuery(Connectors.fromFlowQuery(flowRef, dsd.getRef()), resource.toString(), null, null, serieskeysonly, null, false),
                    SdmxMediaType.STRUCTURE_SPECIFIC_DATA_21);
        }

        private Parser<List<Series>> getCompactData21Parser(DataStructure dsd) {
            return (r, l) -> {
                try (DataCursor cursor = SdmxXmlStreams.compactData21(dsd, dialect).parse(new XMLEventStreamReader(r), () -> {
                })) {
                    return SeriesSupport.copyOf(cursor);
                } catch (IOException ex) {
                    throw new SdmxIOException("Cannot parse compact data 21", ex);
                }
            };
        }
    }
}
