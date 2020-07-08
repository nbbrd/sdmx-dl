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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author Philippe Charles
 */
public interface RestClient {

    @NonNull
    Response open(@NonNull URL query, @NonNull String mediaType, @NonNull String langs) throws IOException;

    interface Response extends Closeable {

        @NonNull
        String getContentType() throws IOException;

        @NonNull
        InputStream getBody() throws IOException;
    }

    @lombok.Getter
    final class ResponseError extends IOException {

        private final int responseCode;
        private final String responseMessage;
        private final Map<String, List<String>> headerFields;

        public ResponseError(int responseCode, String responseMessage, Map<String, List<String>> headerFields) {
            super(responseCode + ": " + responseMessage);
            this.responseCode = responseCode;
            this.responseMessage = responseMessage;
            this.headerFields = headerFields;
        }
    }
}
