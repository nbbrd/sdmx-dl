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

import be.nbb.sdmx.facade.util.TtlCache;
import be.nbb.sdmx.facade.util.TtlCache.Clock;
import java.io.File;
import java.io.IOException;
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
    private final String key;

    CachedFileSdmxConnection(File data, XMLInputFactory factory, SdmxDecoder decoder, ConcurrentMap cache, Clock clock, long ttlInMillis) {
        super(data, factory, decoder);
        this.cache = cache;
        this.clock = clock;
        this.ttlInMillis = ttlInMillis;
        this.key = data.getPath();
    }

    @Override
    protected SdmxDecoder.Info decode() throws IOException {
        SdmxDecoder.Info result = TtlCache.get(cache, key, clock);
        if (result == null) {
            result = super.decode();
            TtlCache.put(cache, key, result, ttlInMillis, clock);
        }
        return result;
    }
}
