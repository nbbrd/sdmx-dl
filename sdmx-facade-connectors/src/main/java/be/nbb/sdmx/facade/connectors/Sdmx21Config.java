/*
 * Copyright 2016 National Bank of Belgium
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
package be.nbb.sdmx.facade.connectors;

import java.util.Map;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder
class Sdmx21Config {

    boolean needsCredentials;
    boolean needsURLEncoding;
    boolean supportsCompression;
    boolean seriesKeysOnlySupported;

    static Sdmx21Config load(Map<?, ?> p) {
        return new Sdmx21Config(
                Util.NEEDS_CREDENTIALS.get(p, false),
                Util.NEEDS_URL_ENCODING.get(p, false),
                Util.SUPPORTS_COMPRESSION.get(p, false),
                Util.SERIES_KEYS_ONLY_SUPPORTED.get(p, false));
    }

    static void store(Map<String, String> p, Sdmx21Config c) {
        Util.NEEDS_CREDENTIALS.set(p, c.isNeedsCredentials());
        Util.NEEDS_URL_ENCODING.set(p, c.isNeedsURLEncoding());
        Util.SUPPORTS_COMPRESSION.set(p, c.isSupportsCompression());
        Util.SERIES_KEYS_ONLY_SUPPORTED.set(p, c.isSeriesKeysOnlySupported());
    }

    static class Sdmx21ConfigBuilder {

        public Sdmx21ConfigBuilder clear() {
            needsCredentials(false);
            needsURLEncoding(false);
            seriesKeysOnlySupported(false);
            supportsCompression(false);
            return this;
        }
    }
}
