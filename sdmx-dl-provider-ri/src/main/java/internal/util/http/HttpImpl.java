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
package internal.util.http;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class HttpImpl {

    enum Authenticators implements HttpAuthenticator {
        NONE {
            @Override
            public @Nullable PasswordAuthentication getPasswordAuthentication(@NonNull URL url) {
                return null;
            }

            @Override
            public void invalidate(@NonNull URL url) {
            }
        }
    }

    enum EventListeners implements HttpEventListener {
        NONE {
            @Override
            public void onOpen(@NonNull HttpRequest request, @NonNull Proxy proxy, @NonNull HttpAuthScheme scheme) {
            }

            @Override
            public void onSuccess(@NonNull MediaType mediaType) {
            }

            @Override
            public void onRedirection(@NonNull URL oldUrl, @NonNull URL newUrl) {
            }

            @Override
            public void onUnauthorized(@NonNull URL url, @NonNull HttpAuthScheme oldScheme, @NonNull HttpAuthScheme newScheme) {
            }

            @Override
            public void onEvent(@NonNull String message) {
            }
        }
    }

    enum StreamDecoders implements StreamDecoder {
        NONE {
            @Override
            public @NonNull InputStream decode(@NonNull InputStream stream) {
                return stream;
            }
        },
        GZIP {
            @Override
            public @NonNull InputStream decode(@NonNull InputStream stream) throws IOException {
                return new GZIPInputStream(stream);
            }
        },
        DEFLATE {
            @Override
            public @NonNull InputStream decode(@NonNull InputStream stream) {
                return new InflaterInputStream(stream);
            }
        };

        @Override
        public @NonNull String getName() {
            return name().toLowerCase();
        }
    }
}
