/*
 * Copyright 2018 National Bank of Belgium
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
package sdmxdl.provider.ri.http;

import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.io.http.HttpHeaders;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.net.MediaType;
import sdmxdl.Languages;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public final class HttpManager {

    private static final List<HttpDecoration> DECORATIONS = Arrays.asList(
            new ByteCountingDecoration(),
            new DumpingDecoration(),
            new ThrowingStatusDecoration(),
            new RateLimitingDecoration(),
            new RetryDecoration(),
            new RedirectDecoration(),
            new AuthenticatingDecoration(),
            new LazyDecoration()
    );

    private static HttpFactory decorate(HttpFactory httpFactory) {
        HttpFactory result = httpFactory;
        ListIterator<HttpDecoration> iterator = DECORATIONS.listIterator(DECORATIONS.size());
        while (iterator.hasPrevious()) {
            result = iterator.previous().decorate(result);
        }
        return result;
    }

    private static final HttpFactory DEFAULT_HTTP_FACTORY = decorate(new UrlConnectionHttpFactory());

    private final AtomicReference<HttpFactory> FACTORY = new AtomicReference<>(getDefaultHttpFactory());

    public static @NonNull HttpFactory getHttpFactory() {
        return FACTORY.get();
    }

    public static void setHttpFactory(@NonNull HttpFactory httpFactory) {
        FACTORY.set(httpFactory);
    }

    @StaticFactoryMethod(HttpRequest.class)
    public static @NonNull HttpRequest newHttpRequest(@NonNull URI query, @NonNull List<MediaType> mediaTypes, @NonNull Languages languages) {
        return HttpRequest
                .builder()
                .query(query)
                .headers(HttpHeaders
                        .builder()
                        .mediaTypes(mediaTypes)
                        .languages(languages.toString())
                        .build())
                .build();
    }

    public static HttpFactory getDefaultHttpFactory() {
        return DEFAULT_HTTP_FACTORY;
    }
}
