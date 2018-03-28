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

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.web.SdmxWebSource;
import be.nbb.sdmx.facade.util.HasCache;
import be.nbb.sdmx.facade.Series;
import be.nbb.sdmx.facade.util.Property;
import be.nbb.sdmx.facade.util.SdmxMediaType;
import be.nbb.sdmx.facade.util.SeriesSupport;
import static be.nbb.sdmx.facade.web.SdmxWebProperty.*;
import be.nbb.sdmx.facade.xml.stream.SdmxXmlStreams;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxIOException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import internal.connectors.ConnectorRestClient;
import internal.connectors.HasDataCursor;
import internal.connectors.HasSeriesKeysOnlySupported;
import internal.connectors.Connectors;
import static internal.connectors.Connectors.*;
import internal.org.springframework.util.xml.XMLEventStreamReader;
import internal.web.SdmxWebDriverSupport;
import it.bancaditalia.oss.sdmx.client.Parser;
import java.net.URI;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SdmxWebDriver.class)
public final class Sdmx21Driver implements SdmxWebDriver, HasCache {

    private static final String NAME = "sdmx21@connectors";

    @lombok.experimental.Delegate
    private final SdmxWebDriverSupport support = SdmxWebDriverSupport
            .builder()
            .name(NAME)
            .client(ConnectorRestClient.of(Sdmx21Client::new))
            .source(SdmxWebSource
                    .builder()
                    .name("ECB")
                    .description("European Central Bank")
                    .driver(NAME)
                    .endpointOf("https://sdw-wsrest.ecb.europa.eu/service")
                    .propertyOf(SUPPORTS_COMPRESSION_PROPERTY, true)
                    .propertyOf(SERIES_KEYS_ONLY_SUPPORTED_PROPERTY, true)
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("ISTAT")
                    .description("Istituto Nazionale di Statistica")
                    .driver(NAME)
                    .endpointOf("http://sdmx.istat.it/SDMXWS/rest")
                    .propertyOf(SUPPORTS_COMPRESSION_PROPERTY, true)
                    .propertyOf(SERIES_KEYS_ONLY_SUPPORTED_PROPERTY, true)
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("UNDATA")
                    .description("Data access system to UN databases")
                    .driver(NAME)
                    .endpointOf("http://data.un.org/WS/rest")
                    .propertyOf(SERIES_KEYS_ONLY_SUPPORTED_PROPERTY, true)
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("WITS")
                    .description("World Integrated Trade Solutions")
                    .driver(NAME)
                    .endpointOf("http://wits.worldbank.org/API/V1/SDMX/V21/rest")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("INEGI")
                    .description("Instituto Nacional de Estadistica y Geografia")
                    .driver(NAME)
                    .endpointOf("http://sdmx.snieg.mx/service/Rest")
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("IMF_SDMX_CENTRAL")
                    .description("International Monetary Fund SDMX Central")
                    .driver(NAME)
                    .endpointOf("https://sdmxcentral.imf.org/ws/public/sdmxapi/rest")
                    .propertyOf(SUPPORTS_COMPRESSION_PROPERTY, true)
                    .propertyOf(SERIES_KEYS_ONLY_SUPPORTED_PROPERTY, true)
                    .build())
            .source(SdmxWebSource
                    .builder()
                    .name("WB")
                    .description("World Bank")
                    .driver(NAME)
                    .endpointOf("https://api.worldbank.org/v2/sdmx/rest")
                    .propertyOf(SUPPORTS_COMPRESSION_PROPERTY, true)
                    .build())
            .build();

    private final static class Sdmx21Client extends RestSdmxClient implements HasDataCursor, HasSeriesKeysOnlySupported {

        private final boolean seriesKeysOnlySupported;

        private Sdmx21Client(URI endpoint, Map<?, ?> p) {
            super("", endpoint,
                    Property.get(NEEDS_CREDENTIALS_PROPERTY, DEFAULT_NEEDS_CREDENTIALS, p),
                    Property.get(NEEDS_URL_ENCODING_PROPERTY, DEFAULT_NEEDS_URL_ENCODING, p),
                    Property.get(SUPPORTS_COMPRESSION_PROPERTY, DEFAULT_SUPPORTS_COMPRESSION, p));
            this.seriesKeysOnlySupported = Property.get(SERIES_KEYS_ONLY_SUPPORTED_PROPERTY, DEFAULT_SERIES_KEYS_ONLY_SUPPORTED, p);
        }

        @Override
        public DataCursor getDataCursor(DataflowRef flowRef, DataStructure dsd, Key resource, boolean serieskeysonly) throws SdmxException, IOException {
            // FIXME: avoid in-memory copy
            return SeriesSupport.asCursor(getData(flowRef, dsd, resource, serieskeysonly), resource);
        }

        @Override
        public boolean isSeriesKeysOnlySupported() {
            return seriesKeysOnlySupported;
        }

        private List<Series> getData(DataflowRef flowRef, DataStructure dsd, Key resource, boolean serieskeysonly) throws SdmxException {
            return runQuery(getCompactData21Parser(dsd),
                    buildDataQuery(Connectors.fromFlowQuery(flowRef, dsd.getRef()), resource.toString(), null, null, serieskeysonly, null, false),
                    SdmxMediaType.STRUCTURE_SPECIFIC_DATA_21);
        }

        private Parser<List<Series>> getCompactData21Parser(DataStructure dsd) {
            return (r, l) -> {
                try (DataCursor cursor = SdmxXmlStreams.compactData21(dsd).parse(new XMLEventStreamReader(r), () -> {
                })) {
                    return SeriesSupport.copyOf(cursor);
                } catch (IOException ex) {
                    throw new SdmxIOException("Cannot parse compact data 21", ex);
                }
            };
        }
    }
}
