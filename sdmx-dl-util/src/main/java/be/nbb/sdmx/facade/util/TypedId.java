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
package be.nbb.sdmx.facade.util;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import be.nbb.sdmx.facade.SdmxCache;
import java.time.Duration;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
@lombok.EqualsAndHashCode
public final class TypedId<T> {

    @NonNull
    public static <T> TypedId<T> of(@NonNull String content) {
        Objects.requireNonNull(content);
        return new TypedId<>(content);
    }

    private final String content;

    private TypedId(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return content;
    }

    @NonNull
    public TypedId<T> with(@NonNull Object o) {
        Objects.requireNonNull(o);
        return new TypedId<>(content + o);
    }

    @Nullable
    public T load(@NonNull SdmxCache cache) {
        return (T) cache.get(this);
    }

    public void store(@NonNull SdmxCache cache, @NonNull T value, Duration ttl) {
        cache.put(this, value, ttl);
    }
}
