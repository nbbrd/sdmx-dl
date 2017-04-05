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
import be.nbb.sdmx.facade.util.TtlCache.Clock;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;
import javax.xml.stream.XMLInputFactory;

/**
 *
 * @author Philippe Charles
 */
final class CachedFileSdmxConnection extends FileSdmxConnection {

    private final ConcurrentMap cache;
    private final Clock clock;
    private final long ttlInMillis;
    private final String decodeKey;
    private final String loadDataKey;

    CachedFileSdmxConnection(File data, XMLInputFactory factory, SdmxDecoder decoder, ConcurrentMap cache, Clock clock, long ttlInMillis) {
        super(data, factory, decoder);
        this.cache = cache;
        this.clock = clock;
        this.ttlInMillis = ttlInMillis;
        this.decodeKey = data.getPath() + "decode";
        this.loadDataKey = data.getPath() + "loadData";
    }

    @Override
    protected SdmxDecoder.Info decode() throws IOException {
        SdmxDecoder.Info result = TtlCache.get(cache, decodeKey, clock);
        if (result == null) {
            result = super.decode();
            TtlCache.put(cache, decodeKey, result, ttlInMillis, clock);
        }
        return result;
    }

    @Override
    protected DataCursor loadData(SdmxDecoder.Info entry, DataflowRef flowRef, Key key, boolean serieskeysonly) throws IOException {
        if (serieskeysonly) {
            MemSdmxRepository result = TtlCache.get(cache, loadDataKey, clock);
            if (result == null) {
                result = copyOfKeys(flowRef, super.loadData(entry, flowRef, key, serieskeysonly));
                TtlCache.put(cache, loadDataKey, result, ttlInMillis, clock);
            }
            return result.asConnection().getData(flowRef, key, serieskeysonly);
        }
        return super.loadData(entry, flowRef, key, serieskeysonly);
    }

    private static MemSdmxRepository copyOfKeys(DataflowRef flowRef, DataCursor cursor) throws IOException {
        MemSdmxRepository.Builder result = MemSdmxRepository.builder().name("");
        while (cursor.nextSeries()) {
            result.series(Series.of(flowRef, cursor.getKey(), cursor.getTimeFormat(), Collections.emptyList()));
        }
        return result.build();
    }
}
