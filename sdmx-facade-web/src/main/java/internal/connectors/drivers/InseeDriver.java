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
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.web.SdmxWebEntryPoint;
import be.nbb.sdmx.facade.repo.Series;
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.util.SdmxMediaType;
import be.nbb.sdmx.facade.xml.stream.SdmxXmlStreams;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxIOException;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import org.openide.util.lookup.ServiceProvider;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.connectors.HasDataCursor;
import internal.connectors.HasSeriesKeysOnlySupported;
import internal.connectors.ConnectorsDriverSupport;
import internal.connectors.Util;
import internal.xml.stream.InseeDataFactory;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxWebDriver.class)
public final class InseeDriver implements SdmxWebDriver, HasCache {

    private static final String PREFIX = "sdmx:insee:";

    private final XMLInputFactory xml = XMLInputFactory.newInstance();

    @lombok.experimental.Delegate
    private final ConnectorsDriverSupport support = ConnectorsDriverSupport.of(PREFIX, (u, i, l) -> new InseeClient(u, l, xml));

    @Override
    public Collection<SdmxWebEntryPoint> getDefaultEntryPoints() {
        return ConnectorsDriverSupport.entry("INSEE", "Institut national de la statistique et des études économiques", "sdmx:insee:http://bdm.insee.fr/series/sdmx");
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private final static class InseeClient extends RestSdmxClient implements HasDataCursor, HasSeriesKeysOnlySupported {

        private final XMLInputFactory xmlFactory;
        private final InseeDataFactory dataFactory;

        private InseeClient(URL endpoint, LanguagePriorityList langs, XMLInputFactory xmlFactory) {
            super("", endpoint, false, false, true);
            this.languages = Util.fromLanguages(langs);
            this.xmlFactory = xmlFactory;
            this.dataFactory = new InseeDataFactory();
        }

        @Override
        public DataCursor getDataCursor(Dataflow dataflow, DataFlowStructure dsd, Key resource, boolean serieskeysonly) throws SdmxException, IOException {
            String query = buildDataQuery(dataflow, resource.toString(), null, null, serieskeysonly, null, false);
            // FIXME: avoid in-memory copy
            List<Series> data = runQuery((r, l) -> parse(r, Util.toDataStructure(dsd)), query, SdmxMediaType.STRUCTURE_SPECIFIC_DATA_21);
            return Series.asCursor(data, resource);
        }

        @Override
        public boolean isSeriesKeysOnlySupported() {
            return true;
        }

        private List<Series> parse(Reader xmlReader, DataStructure dsd) throws XMLStreamException, SdmxException {
            try {
                return Series.copyOf(SdmxXmlStreams.compactData21(dsd, dataFactory).get(xmlFactory, xmlReader));
            } catch (IOException ex) {
                throw new SdmxIOException("Cannot parse compact data 21", ex);
            }
        }
    }
    //</editor-fold>
}
