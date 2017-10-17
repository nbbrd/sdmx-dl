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
package be.nbb.sdmx.facade.connectors;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.web.WebEntryPoint;
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.repo.Series;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import org.openide.util.lookup.ServiceProvider;
import be.nbb.sdmx.facade.web.SdmxWebDriver;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxWebDriver.class)
public final class Sdmx21Driver implements SdmxWebDriver, HasCache {

    private static final String PREFIX = "sdmx:sdmx21:";

    private final XMLInputFactory xml = XMLInputFactory.newInstance();

    @lombok.experimental.Delegate
    private final SdmxDriverSupport support = SdmxDriverSupport.of(PREFIX, (u, i, l) -> new Sdmx21Client(u, Sdmx21Config.load(i), l, xml));

    @Override
    public List<WebEntryPoint> getDefaultEntryPoints() {
        Sdmx21EntryPointBuilder b = new Sdmx21EntryPointBuilder();
        List<WebEntryPoint> result = new ArrayList<>();
        result.add(b.clear()
                .name("ECB")
                .description("European Central Bank")
                .endpoint("https://sdw-wsrest.ecb.europa.eu/service")
                .supportsCompression(true)
                .seriesKeysOnlySupported(true)
                .build());
        result.add(b.clear()
                .name("ISTAT")
                .description("Istituto Nazionale di Statistica")
                .endpoint("http://sdmx.istat.it/SDMXWS/rest")
                .supportsCompression(true)
                .seriesKeysOnlySupported(true)
                .build());
        result.add(b.clear()
                .name("UNDATA")
                .description("Data access system to UN databases")
                .endpoint("http://data.un.org/WS/rest")
                .seriesKeysOnlySupported(true)
                .build());
        result.add(b.clear()
                .name("WITS")
                .description("World Integrated Trade Solutions")
                .endpoint("http://wits.worldbank.org/API/V1/SDMX/V21/rest")
                .build());
        result.add(b.clear()
                .name("INEGI")
                .description("Instituto Nacional de Estadistica y Geografia")
                .endpoint("http://sdmx.snieg.mx/service/Rest")
                .build());
        result.add(b.clear()
                .name("IMF_SDMX_CENTRAL")
                .description("International Monetary Fund SDMX Central")
                .endpoint("https://sdmxcentral.imf.org/ws/public/sdmxapi/rest")
                .supportsCompression(true)
                .seriesKeysOnlySupported(true)
                .build());
        return result;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final class Sdmx21EntryPointBuilder {

        private String name = null;
        private String description = null;
        private String endpoint = null;
        private final Sdmx21Config.Sdmx21ConfigBuilder config = Sdmx21Config.builder();

        public Sdmx21EntryPointBuilder clear() {
            this.name = null;
            this.description = null;
            this.endpoint = null;
            this.config.clear();
            return this;
        }

        public Sdmx21EntryPointBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Sdmx21EntryPointBuilder description(String description) {
            this.description = description;
            return this;
        }

        public Sdmx21EntryPointBuilder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Sdmx21EntryPointBuilder needsCredentials(boolean needsCredentials) {
            config.needsCredentials(needsCredentials);
            return this;
        }

        public Sdmx21EntryPointBuilder needsURLEncoding(boolean needsURLEncoding) {
            config.needsURLEncoding(needsURLEncoding);
            return this;
        }

        public Sdmx21EntryPointBuilder supportsCompression(boolean supportsCompression) {
            config.supportsCompression(supportsCompression);
            return this;
        }

        public Sdmx21EntryPointBuilder seriesKeysOnlySupported(boolean seriesKeysOnlySupported) {
            config.seriesKeysOnlySupported(seriesKeysOnlySupported);
            return this;
        }

        private Map<String, String> toProperties() {
            Map<String, String> result = new HashMap<>();
            Sdmx21Config.store(result, config.build());
            return result;
        }

        public WebEntryPoint build() {
            return WebEntryPoint.builder()
                    .name(name)
                    .description(description)
                    .uri(PREFIX + endpoint)
                    .properties(toProperties())
                    .build();
        }
    }

    private final static class Sdmx21Client extends RestSdmxClient implements HasDataCursor, HasSeriesKeysOnlySupported {

        private final Sdmx21Config config;
        private final XMLInputFactory factory;

        private Sdmx21Client(URL endpoint, Sdmx21Config config, LanguagePriorityList langs, XMLInputFactory factory) {
            super("", endpoint, config.isNeedsCredentials(), config.isNeedsURLEncoding(), config.isSupportsCompression());
            this.languages = Util.fromLanguages(langs);
            this.config = config;
            this.factory = factory;
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
            return config.isSeriesKeysOnlySupported();
        }

        private List<Series> parse(Reader xmlReader, DataStructure dsd) throws XMLStreamException, SdmxException {
            try {
                return Series.copyOf(SdmxXmlStreams.compactData21(dsd).get(factory, xmlReader));
            } catch (IOException ex) {
                throw new SdmxIOException("Cannot parse compact data 21", ex);
            }
        }
    }
    //</editor-fold>
}
