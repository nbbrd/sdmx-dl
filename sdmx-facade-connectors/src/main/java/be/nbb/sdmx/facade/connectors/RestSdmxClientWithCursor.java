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

import be.nbb.sdmx.facade.util.SdmxMediaType;
import be.nbb.sdmx.facade.util.SdmxParser;
import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.Key;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import it.bancaditalia.oss.sdmx.util.SdmxException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.Value;

/**
 *
 * @author Philippe Charles
 */
final class RestSdmxClientWithCursor extends RestSdmxClient {

    private final Config config;

    public RestSdmxClientWithCursor(@Nonnull String name, @Nonnull URL endpoint, @Nonnull Config config) {
        super(name, endpoint, config.isNeedsCredentials(), config.isNeedsURLEncoding(), config.isSupportsCompression());
        this.config = config;
    }

    @Nonnull
    public DataCursor getDataCursor(@Nonnull Dataflow dataflow, @Nonnull DataFlowStructure dsd, @Nonnull Key resource, boolean serieskeysonly) throws SdmxException, IOException {
        String query = buildDataQuery(dataflow, resource.toString(), null, null, serieskeysonly, null, false);
        InputStreamReader stream = runQuery(query, SdmxMediaType.STRUCTURE_SPECIFIC_DATA_21);
        if (stream == null) {
            throw new SdmxException("The query returned a null stream");
        }
        return SdmxParser.getDefault().compactData21(stream, Util.toDataStructure(dsd));
    }

    @Nonnull
    Config getConfig() {
        return config;
    }

    @Value
    @Builder
    static class Config {

        private boolean needsCredentials;
        private boolean needsURLEncoding;
        private boolean supportsCompression;
        private boolean seriesKeysOnlySupported;
    }
}
