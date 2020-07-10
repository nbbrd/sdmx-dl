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
package sdmxdl.util;

import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.ext.SdmxCache;
import sdmxdl.repo.SdmxRepository;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

/**
 * @param <T>
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.EqualsAndHashCode
public final class TypedId<T> {

    @NonNull
    public static <T> TypedId<T> of(
            @NonNull String content,
            @NonNull Function<SdmxRepository, T> loader,
            @NonNull Function<T, SdmxRepository> storer) {
        Objects.requireNonNull(content);
        Objects.requireNonNull(loader);
        Objects.requireNonNull(storer);
        return new TypedId<>(content, loader, storer);
    }

    @lombok.Getter
    private final String content;
    private final Function<SdmxRepository, T> loader;
    private final Function<T, SdmxRepository> storer;

    @NonNull
    public TypedId<T> with(@NonNull Object o) {
        Objects.requireNonNull(o);
        return new TypedId<>(content + o, loader, storer);
    }

    @Nullable
    public T load(@NonNull SdmxCache cache) {
        SdmxRepository repo = cache.get(content);
        return repo != null ? loader.apply(repo) : null;
    }

    public void store(@NonNull SdmxCache cache, @NonNull T value, Duration ttl) {
        cache.put(content, storer.apply(value), ttl);
    }
}
