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
package internal.connectors.drivers;

import be.nbb.sdmx.facade.util.Property;
import static be.nbb.sdmx.facade.web.SdmxWebProperty.*;
import static internal.connectors.Connectors.*;
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
                Property.get(NEEDS_CREDENTIALS_PROPERTY, DEFAULT_NEEDS_CREDENTIALS, p),
                Property.get(NEEDS_URL_ENCODING_PROPERTY, DEFAULT_NEEDS_URL_ENCODING, p),
                Property.get(SUPPORTS_COMPRESSION_PROPERTY, DEFAULT_SUPPORTS_COMPRESSION, p),
                Property.get(SERIES_KEYS_ONLY_SUPPORTED_PROPERTY, DEFAULT_SERIES_KEYS_ONLY_SUPPORTED, p));
    }

    static void store(Map<String, String> p, Sdmx21Config c) {
        Property.set(NEEDS_CREDENTIALS_PROPERTY, c.isNeedsCredentials(), p);
        Property.set(NEEDS_URL_ENCODING_PROPERTY, c.isNeedsURLEncoding(), p);
        Property.set(SUPPORTS_COMPRESSION_PROPERTY, c.isSupportsCompression(), p);
        Property.set(SERIES_KEYS_ONLY_SUPPORTED_PROPERTY, c.isSeriesKeysOnlySupported(), p);
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
