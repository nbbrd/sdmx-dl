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
package be.nbb.sdmx.facade.web;

import java.util.concurrent.TimeUnit;

/**
 *
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

    /**
     * Defines the timeout value (in milliseconds) to be used when reading an
     * input stream from an URL connection. A timeout of zero is interpreted as
     * an infinite timeout. Default value is 2 minutes.
     */
    public final String READ_TIMEOUT_PROPERTY = "readTimeout";
    public final int DEFAULT_READ_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(2);

    /**
     * Defines the duration (in milliseconds) of response storage in the cache.
     * A duration of zero is interpreted as an infinite duration. Default value
     * is 5 minutes.
     */
    public final String CACHE_TTL_PROPERTY = "cacheTtl";
    public final long DEFAULT_CACHE_TTL = TimeUnit.MINUTES.toMillis(5);

    /**
     * Defines the max number of redirects to be followed by HTTP client. This
     * limit is intended to prevent infinite loop. Default value is 5.
     */
    public final String MAX_REDIRECTS_PROPERTY = "maxRedirects";
    public final int DEFAULT_MAX_REDIRECTS = 5;

    /**
     * Defines if series-keys-only query is supported. Default value is false.
     */
    public final String SERIES_KEYS_ONLY_SUPPORTED_PROPERTY = "seriesKeysOnlySupported";
    public final boolean DEFAULT_SERIES_KEYS_ONLY_SUPPORTED = false;
}
