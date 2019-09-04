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
package internal.file;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataFilter;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.file.SdmxFileSet;
import be.nbb.sdmx.facade.Series;
import be.nbb.sdmx.facade.parser.DataFactory;
import be.nbb.sdmx.facade.util.TypedId;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import be.nbb.sdmx.facade.SdmxCache;
import java.time.Duration;

/**
 *
 * @author Philippe Charles
 */
public final class CachedResource extends SdmxDecoderResource {

    // TODO: replace ttl with file last modification time
    private static final Duration DEFAULT_CACHE_TTL = Duration.ofMinutes(5);

    private final SdmxCache cache;
    private final TypedId<SdmxDecoder.Info> decodeKey;
    private final TypedId<List<Series>> loadDataKey;

    public CachedResource(SdmxFileSet files, LanguagePriorityList languages, SdmxDecoder decoder, Optional<DataFactory> dataFactory, SdmxCache cache) {
        super(files, languages, decoder, dataFactory);
        this.cache = cache;
        String base = SdmxFileUtil.toXml(files) + languages.toString();
        this.decodeKey = TypedId.of("decode://" + base);
        this.loadDataKey = TypedId.of("loadData://" + base);
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
