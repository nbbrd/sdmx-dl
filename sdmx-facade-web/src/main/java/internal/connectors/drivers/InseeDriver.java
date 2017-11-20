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
import be.nbb.sdmx.facade.Series;
import be.nbb.sdmx.facade.parser.spi.SdmxDialect;
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.util.SdmxFix;
import be.nbb.sdmx.facade.util.SdmxMediaType;
import be.nbb.sdmx.facade.util.SeriesSupport;
import be.nbb.sdmx.facade.xml.stream.SdmxXmlStreams;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxIOException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.connectors.HasDataCursor;
import internal.connectors.HasSeriesKeysOnlySupported;
import internal.connectors.ConnectorsDriverSupport;
import internal.connectors.Util;
import internal.org.springframework.util.xml.XMLEventStreamReader;
import internal.util.drivers.InseeDialect;
import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.client.Parser;
import java.net.URI;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxWebDriver.class)
public final class InseeDriver implements SdmxWebDriver, HasCache {

    private static final String PREFIX = "sdmx:insee:";

    @lombok.experimental.Delegate
    private final ConnectorsDriverSupport support = ConnectorsDriverSupport.of(PREFIX, (u, i, l) -> new InseeClient(u, l));

    @SdmxFix(id = "INSEE#1", cause = "Fallback to http due to some servers that use root certificate unknown to jdk'")
    @Override
    public Collection<SdmxWebEntryPoint> getDefaultEntryPoints() {
        return ConnectorsDriverSupport.entry("INSEE", "Institut national de la statistique et des études économiques", "sdmx:insee:http://bdm.insee.fr/series/sdmx");
    }

    private final static class InseeClient extends RestSdmxClient implements HasDataCursor, HasSeriesKeysOnlySupported {

        @SdmxFix(id = "INSEE#2", cause = "Does not follow sdmx standard codes")
        private final SdmxDialect dialect;

        private InseeClient(URI endpoint, LanguagePriorityList langs) {
            super("", endpoint, false, false, true);
            this.languages = Util.fromLanguages(langs);
            this.dialect = new InseeDialect();
        }

        @Override
        public DataFlowStructure getDataFlowStructure(DSDIdentifier dsd, boolean full) throws SdmxException {
            DataFlowStructure result = super.getDataFlowStructure(dsd, full);
            fixMissingCodes(result);
            return result;
        }

        @Override
        public DataCursor getDataCursor(Dataflow dataflow, DataFlowStructure dsd, Key resource, boolean serieskeysonly) throws SdmxException, IOException {
            // FIXME: avoid in-memory copy
            return SeriesSupport.asCursor(getData(dataflow, dsd, resource, serieskeysonly), resource);
        }

        @Override
        public boolean isSeriesKeysOnlySupported() {
            return true;
        }

        @SdmxFix(id = "INSEE#3", cause = "Some codes are missing in dsd even when requested with 'references=children'")
        private void fixMissingCodes(DataFlowStructure dsd) throws SdmxException {
            for (Dimension d : dsd.getDimensions()) {
                Codelist freq = d.getCodeList();
                if (freq.getCodes().isEmpty()) {
                    freq.setCodes(super.getCodes(freq.getId(), freq.getAgency(), freq.getVersion()));
                }
            }
        }

        private List<Series> getData(Dataflow dataflow, DataFlowStructure dsd, Key resource, boolean serieskeysonly) throws SdmxException {
            return runQuery(
                    getCompactData21Parser(dsd),
                    buildDataQuery(dataflow, resource.toString(), null, null, serieskeysonly, null, false),
                    SdmxMediaType.STRUCTURE_SPECIFIC_DATA_21);
        }

        private Parser<List<Series>> getCompactData21Parser(DataFlowStructure dsd) {
            return (r, l) -> {
                DataStructure tmp = Util.toStructure(dsd);
                try (DataCursor cursor = SdmxXmlStreams.compactData21(tmp, dialect).parse(new XMLEventStreamReader(r), () -> {
                })) {
                    return SeriesSupport.copyOf(cursor);
                } catch (IOException ex) {
                    throw new SdmxIOException("Cannot parse compact data 21", ex);
                }
            };
        }
    }
}
