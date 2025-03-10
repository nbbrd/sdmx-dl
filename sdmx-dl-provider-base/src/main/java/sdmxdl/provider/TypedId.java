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
package sdmxdl.provider;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.io.function.IOSupplier;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ext.Cache;
import sdmxdl.web.WebSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @param <T>
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.EqualsAndHashCode
public final class TypedId<T> {

    @NonNull
    public static <T> TypedId<T> of(
            @NonNull URI content,
            @NonNull Function<DataRepository, T> loader,
            @NonNull Function<T, DataRepository> storer) {
        return new TypedId<>(content, loader, storer);
    }

    @NonNull
    @lombok.Getter
    private final URI content;

    @NonNull
    private final Function<DataRepository, T> loader;

    @NonNull
    private final Function<T, DataRepository> storer;

    @NonNull
    public TypedId<T> with(@NonNull Object o) {
        return new TypedId<>(resolveURI(content, o.toString()), loader, storer);
    }

    @Nullable
    public T peek(@NonNull Cache<DataRepository> cache) {
        DataRepository repo = cache.get(content.toString());
        return repo != null ? loader.apply(repo) : null;
    }

    @NonNull
    public T load(@NonNull Cache<DataRepository> cache, @NonNull IOSupplier<T> factory, @NonNull Function<? super T, Duration> ttl) throws IOException {
        return load(cache, factory, ttl, o -> true);
    }

    @NonNull
    public T load(@NonNull Cache<DataRepository> cache, @NonNull IOSupplier<T> factory, @NonNull Function<? super T, Duration> ttl, @NonNull Predicate<? super T> validator) throws IOException {
        T result = peek(cache);
        if (result == null || !validator.test(result)) {
            result = factory.getWithIO();
            cache.put(content.toString(), storer.apply(result).toBuilder().ttl(cache.getClock().instant(), ttl.apply(result)).build());
        }
        return result;
    }

    public static URI resolveURI(URI base, String... items) {
        return URI.create(Stream.of(items)
                .map(item -> {
                    try {
                        return URLEncoder.encode(item, UTF_8.name());
                    } catch (UnsupportedEncodingException ex) {
                        throw new UncheckedIOException(ex);
                    }
                })
                .collect(Collectors.joining("/", base + "/", "")));
    }

    public static String getUniqueID(WebSource source) {
        return source.getId() + "_" + hash(source);
    }

    private static String hash(WebSource source) {
        try {
            return bytesToHex(MessageDigest.getInstance("MD5").digest(source.toString().getBytes(UTF_8))).substring(0, 7);
        } catch (NoSuchAlgorithmException e) {
            int hashCode = source.hashCode();
            return hashCode >= 0 ? "0" + hashCode : "1" + -hashCode;
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder result = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                result.append('0');
            }
            result.append(hex);
        }
        return result.toString();
    }
}
