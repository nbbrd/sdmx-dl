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
package sdmxdl.util.web;

import nbbrd.io.text.Parser;
import sdmxdl.util.Property;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SdmxWebProperty {

    /**
     * Defines the timeout value (in milliseconds) to be used when opening an
     * URL connection. A timeout of zero is interpreted as an infinite timeout.
     * Default value is 2 minutes.
     */
    public final String CONNECT_TIMEOUT_PROPERTY = "connectTimeout";
    public final int DEFAULT_CONNECT_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(2);

    public int getConnectTimeout(Map<?, ?> o) {
        return Property.get(CONNECT_TIMEOUT_PROPERTY, DEFAULT_CONNECT_TIMEOUT, o, Parser.onInteger());
    }

    /**
     * Defines the timeout value (in milliseconds) to be used when reading an
     * input stream from an URL connection. A timeout of zero is interpreted as
     * an infinite timeout. Default value is 2 minutes.
     */
    public final String READ_TIMEOUT_PROPERTY = "readTimeout";
    public final int DEFAULT_READ_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(2);

    public int getReadTimeout(Map<?, ?> o) {
        return Property.get(READ_TIMEOUT_PROPERTY, DEFAULT_READ_TIMEOUT, o, Parser.onInteger());
    }

    /**
     * Defines the duration (in milliseconds) of response storage in the cache.
     * A duration of zero is interpreted as an infinite duration. Default value
     * is 5 minutes.
     */
    public final String CACHE_TTL_PROPERTY = "cacheTtl";
    public final long DEFAULT_CACHE_TTL = TimeUnit.MINUTES.toMillis(5);

    public long getCacheTtl(Map<?, ?> o) {
        return Property.get(CACHE_TTL_PROPERTY, DEFAULT_CACHE_TTL, o, Parser.onLong());
    }

    /**
     * Defines the max number of redirects to be followed by HTTP client. This
     * limit is intended to prevent infinite loop. Default value is 5.
     */
    public final String MAX_REDIRECTS_PROPERTY = "maxRedirects";
    public final int DEFAULT_MAX_REDIRECTS = 5;

    public int getMaxRedirects(Map<?, ?> o) {
        return Property.get(MAX_REDIRECTS_PROPERTY, DEFAULT_MAX_REDIRECTS, o, Parser.onInteger());
    }

    /**
     * Defines if series-keys-only query is supported. Default value is false.
     */
    public final String SERIES_KEYS_ONLY_SUPPORTED_PROPERTY = "seriesKeysOnlySupported";
    public final boolean DEFAULT_SERIES_KEYS_ONLY_SUPPORTED = false;

    public boolean isSeriesKeysOnlySupported(Map<?, ?> o) {
        return Property.get(SERIES_KEYS_ONLY_SUPPORTED_PROPERTY, DEFAULT_SERIES_KEYS_ONLY_SUPPORTED, o, Parser.onBoolean());
    }

    /**
     * Defines if trailing slash is required in queries. Default value is false.
     */
    public final String TRAILING_SLASH_REQUIRED_PROPERTY = "trailingSlashRequired";
    public final boolean DEFAULT_TRAILING_SLASH_REQUIRED = false;

    public boolean isTrailingSlashRequired(Map<?, ?> o) {
        return Property.get(TRAILING_SLASH_REQUIRED_PROPERTY, DEFAULT_TRAILING_SLASH_REQUIRED, o, Parser.onBoolean());
    }

    /**
     * Defines if preemptive authentication should be used. Default value is false.
     */
    public final String PREEMPTIVE_AUTHENTICATION_PROPERTY = "preemptiveAuthentication";
    public final boolean DEFAULT_PREEMPTIVE_AUTHENTICATION = false;

    public boolean isPreemptiveAuthentication(Map<?, ?> o) {
        return Property.get(PREEMPTIVE_AUTHENTICATION_PROPERTY, DEFAULT_PREEMPTIVE_AUTHENTICATION, o, Parser.onBoolean());
    }
}
