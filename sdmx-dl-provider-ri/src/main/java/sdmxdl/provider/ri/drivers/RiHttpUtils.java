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
package sdmxdl.provider.ri.drivers;

import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.io.http.HttpHeaders;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.net.MediaType;
import sdmxdl.Languages;
import sdmxdl.provider.ri.http.*;

import java.net.URI;
import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class RiHttpUtils {

    public static final HttpClientFactory DEFAULT_HTTP_FACTORY =
            new ByteCountingHttpClientDecorator().decorate(
                    new DumpingHttpClientDecorator().decorate(
                            new LazyHttpClientDecorator().decorate(
                                    new UrlConnectionHttpClientFactory()
                            )
                    )
            );

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
}
