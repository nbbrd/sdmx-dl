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
package internal.util.rest;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
public final class RestQueryBuilder {

    @lombok.NonNull
    private final URL endPoint;

    private boolean trailingSlash = false;

    private final Charset encoding = StandardCharsets.UTF_8;
    private final List<String> paths = new ArrayList<>();
    private final Map<String, String> params = new LinkedHashMap<>();

    /**
     * Appends a trailing slash to the final URL.
     *
     * @param trailingSlash
     * @return this builder
     * @see <a href="https://en.wikipedia.org/wiki/URI_normalization">https://en.wikipedia.org/wiki/URI_normalization</a>
     */
    @NonNull
    public RestQueryBuilder trailingSlash(boolean trailingSlash) {
        this.trailingSlash = trailingSlash;
        return this;
    }

    /**
     * Appends the specified path to the current URL.
     *
     * @param path a non-null path
     * @return this builder
     * @throws NullPointerException if path is null
     */
    @NonNull
    public RestQueryBuilder path(@NonNull String path) {
        Objects.requireNonNull(path);
        paths.add(path);
        return this;
    }

    /**
     * Appends the specified path to the current URL.
     *
     * @param path a non-null path
     * @return this builder
     * @throws NullPointerException if path is null
     */
    @NonNull
    public RestQueryBuilder path(@NonNull List<String> path) {
        Objects.requireNonNull(path);
        paths.addAll(path);
        return this;
    }

    /**
     * Appends the specified parameter to the current URL.
     *
     * @param key   a non-null key
     * @param value a non-null value
     * @return this builder
     * @throws NullPointerException if key or value is null
     */
    @NonNull
    public RestQueryBuilder param(@NonNull String key, @NonNull String value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        params.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(endPoint);

        for (String path : paths) {
            result.append('/').append(encode(path, encoding));
        }

        if (trailingSlash) {
            result.append('/');
        }

        boolean first = true;
        for (Map.Entry<String, String> o : params.entrySet()) {
            result.append(first ? '?' : '&');
            result
                    .append(encode(o.getKey(), encoding))
                    .append('=')
                    .append(encode(o.getValue(), encoding));
            first = false;
        }

        return result.toString();
    }

    /**
     * Creates a new URL using the specified path and parameters.
     *
     * @return a URL
     * @throws java.net.MalformedURLException
     */
    @NonNull
    public URL build() throws MalformedURLException {
        return new URL(toString());
    }

    private static String encode(String s, Charset charset) {
        try {
            return URLEncoder.encode(s, charset.name());
        } catch (UnsupportedEncodingException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
