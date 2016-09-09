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
import be.nbb.sdmx.facade.Key;
import static be.nbb.sdmx.facade.connectors.Util.NEEDS_CREDENTIALS;
import static be.nbb.sdmx.facade.connectors.Util.NEEDS_URL_ENCODING;
import static be.nbb.sdmx.facade.connectors.Util.SERIES_KEYS_ONLY_SUPPORTED;
import static be.nbb.sdmx.facade.connectors.Util.SUPPORTS_COMPRESSION;
import be.nbb.sdmx.facade.driver.SdmxDriver;
import be.nbb.sdmx.facade.driver.WsEntryPoint;
import be.nbb.sdmx.facade.util.SdmxMediaType;
import be.nbb.sdmx.facade.util.XMLStreamCompactDataCursor21;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.util.SdmxException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Properties;
import javax.xml.stream.XMLInputFactory;
import org.openide.util.lookup.ServiceProvider;
import be.nbb.sdmx.facade.util.HasCache;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxDriver.class)
public final class Sdmx21Driver extends SdmxDriver implements HasCache {

    private static final String PREFIX = "sdmx:sdmx21:";

    @lombok.experimental.Delegate
    private final SdmxDriverSupport support = SdmxDriverSupport.of(PREFIX, new SdmxDriverSupport.ClientSupplier() {
        @Override
        public GenericSDMXClient getClient(URL endpoint, Properties info) throws MalformedURLException {
            return new ExtRestSdmxClient("", endpoint, load(info));
        }
    });

    @Override
    public List<WsEntryPoint> getDefaultEntryPoints() {
        return asList(
                of("ECB", "European Central Bank", "sdmx:sdmx21:https://sdw-wsrest.ecb.europa.eu/service", Config.builder().supportsCompression(true).seriesKeysOnlySupported(true).build()),
                of("ISTAT", "Istituto Nazionale di Statistica", "sdmx:sdmx21:http://sdmx.istat.it/SDMXWS/rest", Config.builder().supportsCompression(true).seriesKeysOnlySupported(true).build()),
                of("INSEE", "Institut national de la statistique et des études économiques", "sdmx:sdmx21:http://www.bdm.insee.fr/series/sdmx", Config.builder().seriesKeysOnlySupported(true).build()),
                of("UNDATA", "Data access system to UN databases", "sdmx:sdmx21:http://data.un.org/WS/rest", Config.builder().seriesKeysOnlySupported(true).build()),
                of("WITS", "World Integrated Trade Solutions", "sdmx:sdmx21:http://wits.worldbank.org/API/V1/SDMX/V21/rest", Config.builder().seriesKeysOnlySupported(false).build())
        );
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static WsEntryPoint of(String name, String description, String url, Config c) {
        Properties p = new Properties();
        store(p, c);
        return WsEntryPoint.of(name, description, url, p);
    }

    private static Config load(Properties p) {
        return new Config(
                NEEDS_CREDENTIALS.get(p, false),
                NEEDS_URL_ENCODING.get(p, false),
                SUPPORTS_COMPRESSION.get(p, false),
                SERIES_KEYS_ONLY_SUPPORTED.get(p, false));
    }

    private static void store(Properties p, Config c) {
        NEEDS_CREDENTIALS.set(p, c.isNeedsCredentials());
        NEEDS_URL_ENCODING.set(p, c.isNeedsURLEncoding());
        SUPPORTS_COMPRESSION.set(p, c.isSupportsCompression());
        SERIES_KEYS_ONLY_SUPPORTED.set(p, c.isSeriesKeysOnlySupported());
    }

    @lombok.Value
    @lombok.Builder
    private static class Config {

        boolean needsCredentials;
        boolean needsURLEncoding;
        boolean supportsCompression;
        boolean seriesKeysOnlySupported;
    }

    private final static class ExtRestSdmxClient extends RestSdmxClient implements HasDataCursor, HasSeriesKeysOnlySupported {

        private final Config config;
        private final XMLInputFactory factory;

        public ExtRestSdmxClient(String name, URL endpoint, Config config) {
            super(name, endpoint, config.isNeedsCredentials(), config.isNeedsURLEncoding(), config.isSupportsCompression());
            this.config = config;
            this.factory = XMLInputFactory.newInstance();
        }

        @Override
        public DataCursor getDataCursor(Dataflow dataflow, DataFlowStructure dsd, Key resource, boolean serieskeysonly) throws SdmxException, IOException {
            String query = buildDataQuery(dataflow, resource.toString(), null, null, serieskeysonly, null, false);
            InputStreamReader stream = runQuery(query, SdmxMediaType.STRUCTURE_SPECIFIC_DATA_21);
            if (stream == null) {
                throw new SdmxException("The query returned a null stream");
            }
            return XMLStreamCompactDataCursor21.compactData21(factory, stream, Util.toDataStructure(dsd));
        }

        @Override
        public boolean isSeriesKeysOnlySupported() {
            return config.isSeriesKeysOnlySupported();
        }
    }
    //</editor-fold>
}
