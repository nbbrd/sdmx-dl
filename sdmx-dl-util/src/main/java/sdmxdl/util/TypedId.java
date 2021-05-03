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
import nbbrd.io.function.IOSupplier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.ext.SdmxCache;
import sdmxdl.repo.SdmxRepository;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

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
        return new TypedId<>(content, loader, storer);
    }

    @lombok.NonNull
    @lombok.Getter
    private final String content;

    @lombok.NonNull
    private final Function<SdmxRepository, T> loader;

    @lombok.NonNull
    private final Function<T, SdmxRepository> storer;

    @NonNull
    public TypedId<T> with(@NonNull Object o) {
        Objects.requireNonNull(o);
        return new TypedId<>(content + o, loader, storer);
    }

    @Nullable
    public T peek(@NonNull SdmxCache cache) {
        SdmxRepository repo = cache.get(content);
        return repo != null ? loader.apply(repo) : null;
    }

    @NonNull
    public T load(@NonNull SdmxCache cache, @NonNull IOSupplier<T> factory, @NonNull Function<? super T, Duration> ttl) throws IOException {
        return load(cache, factory, ttl, o -> true);
    }

    @NonNull
    public T load(@NonNull SdmxCache cache, @NonNull IOSupplier<T> factory, @NonNull Function<? super T, Duration> ttl, @NonNull Predicate<? super T> validator) throws IOException {
        T result = peek(cache);
        if (result == null || !validator.test(result)) {
            result = factory.getWithIO();
            cache.put(content, storer.apply(result), ttl.apply(result));
        }
        return result;
    }
}
