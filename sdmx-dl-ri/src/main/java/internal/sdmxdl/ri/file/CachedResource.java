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
package internal.sdmxdl.ri.file;

import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.util.TypedId;
import sdmxdl.xml.XmlFileSource;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
public final class CachedResource extends SdmxDecoderResource {

    // TODO: replace ttl with file last modification time
    private static final Duration DEFAULT_CACHE_TTL = Duration.ofMinutes(5);

    private final SdmxCache cache;
    private final TypedId<SdmxDecoder.Info> decodeKey;
    private final TypedId<List<Series>> loadDataKey;

    public CachedResource(SdmxFileSource source, LanguagePriorityList languages, SdmxDecoder decoder, Optional<ObsFactory> dataFactory, SdmxCache cache) {
        super(source, languages, decoder, dataFactory);
        this.cache = cache;
        String base = getBase(source, languages);
        this.decodeKey = TypedId.of("decode://" + base);
        this.loadDataKey = TypedId.of("loadData://" + base);
    }

    private static String getBase(SdmxFileSource source, LanguagePriorityList languages) {
        try {
            return XmlFileSource.getFormatter().formatToString(source) + languages.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public SdmxDecoder.Info decode() throws IOException {
        SdmxDecoder.Info result = decodeKey.load(cache);
        if (result == null) {
            result = super.decode();
            decodeKey.store(cache, result, DEFAULT_CACHE_TTL);
        }
        return result;
    }

    @Override
    public DataCursor loadData(SdmxDecoder.Info entry, DataflowRef flowRef, Key key, boolean serieskeysonly) throws IOException {
        if (serieskeysonly) {
            List<Series> result = loadDataKey.load(cache);
            if (result == null) {
                result = copyOfKeysAndMeta(entry, flowRef, key);
                loadDataKey.store(cache, result, DEFAULT_CACHE_TTL);
            }
            return DataCursor.of(result, key);
        }
        return super.loadData(entry, flowRef, key, serieskeysonly);
    }

    private List<Series> copyOfKeysAndMeta(SdmxDecoder.Info entry, DataflowRef flowRef, Key key) throws IOException {
        try (DataCursor c = super.loadData(entry, flowRef, key, true)) {
            return c.toStream(DataFilter.Detail.NO_DATA).collect(Collectors.toList());
        }
    }
}
