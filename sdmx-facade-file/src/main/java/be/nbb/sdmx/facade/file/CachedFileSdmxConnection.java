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
package be.nbb.sdmx.facade.file;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.Key;
import be.nbb.sdmx.facade.util.MemSdmxRepository;
import be.nbb.sdmx.facade.util.MemSdmxRepository.Series;
import be.nbb.sdmx.facade.util.TtlCache;
import be.nbb.sdmx.facade.util.TypedId;
import java.io.IOException;
import java.time.Clock;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.xml.stream.XMLInputFactory;

/**
 *
 * @author Philippe Charles
 */
final class CachedFileSdmxConnection extends FileSdmxConnection {

    // TODO: replace ttl with file last modification time
    private static final long DEFAULT_CACHE_TTL = TimeUnit.MINUTES.toMillis(5);
    private static final Clock CLOCK = Clock.systemDefaultZone();

    private final TtlCache cache;
    private final TypedId<SdmxDecoder.Info> decodeKey;
    private final TypedId<MemSdmxRepository> loadDataKey;

    CachedFileSdmxConnection(SdmxFile file, XMLInputFactory factory, SdmxDecoder decoder, String preferredLang, ConcurrentMap cache) {
        super(file, factory, decoder, preferredLang);
        this.cache = TtlCache.of(cache, CLOCK, DEFAULT_CACHE_TTL);
        String id = file.toString();
        this.decodeKey = TypedId.of("cache://" + id + "decode");
        this.loadDataKey = TypedId.of("cache://" + id + "loadData");
    }

    @Override
    protected SdmxDecoder.Info decode() throws IOException {
        SdmxDecoder.Info result = cache.get(decodeKey);
        if (result == null) {
            result = super.decode();
            cache.put(decodeKey, result);
        }
        return result;
    }

    @Override
    protected DataCursor loadData(SdmxDecoder.Info entry, DataflowRef flowRef, Key key, boolean serieskeysonly) throws IOException {
        if (serieskeysonly) {
            MemSdmxRepository result = cache.get(loadDataKey);
            if (result == null) {
                result = copyOfKeys(flowRef, super.loadData(entry, flowRef, key, true));
                cache.put(loadDataKey, result);
            }
            return result.asConnection().getData(flowRef, key, true);
        }
        return super.loadData(entry, flowRef, key, serieskeysonly);
    }

    private static MemSdmxRepository copyOfKeys(DataflowRef flowRef, DataCursor cursor) throws IOException {
        MemSdmxRepository.Builder result = MemSdmxRepository.builder().name("");
        while (cursor.nextSeries()) {
            result.series(Series.of(flowRef, cursor.getSeriesKey(), cursor.getSeriesTimeFormat(), Collections.emptyList(), cursor.getSeriesAttributes()));
        }
        return result.build();
    }
}
