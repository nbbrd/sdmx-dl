/*
 * Copyright 2017 National Bank of Belgium
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
package internal.util;

import be.nbb.sdmx.facade.util.HasCache;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class HasCacheSupport implements HasCache {

    private final Supplier<ConcurrentMap> defaultCache;
    private final AtomicReference<ConcurrentMap> cache;
    private final BiConsumer<ConcurrentMap, ConcurrentMap> onCacheChange;

    @Override
    public ConcurrentMap getCache() {
        return cache.get();
    }

    @Override
    public void setCache(ConcurrentMap cache) {
        ConcurrentMap oldObj = this.cache.get();
        ConcurrentMap newObj = cache != null ? cache : defaultCache.get();
        if (this.cache.compareAndSet(oldObj, newObj)) {
            onCacheChange.accept(oldObj, newObj);
        }
    }
}
