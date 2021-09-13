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

import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.IntProperty;
import nbbrd.io.text.LongProperty;

import java.util.concurrent.TimeUnit;

/**
 * @author Philippe Charles
 */
public final class SdmxWebProperty {

    private SdmxWebProperty() {
        // static class
    }

    /**
     * Defines the timeout value (in milliseconds) to be used when opening an
     * URL connection. A timeout of zero is interpreted as an infinite timeout.
     * Default value is 2 minutes.
     */
    public static final IntProperty CONNECT_TIMEOUT_PROPERTY =
            IntProperty.of("connectTimeout", (int) TimeUnit.MINUTES.toMillis(2));

    /**
     * Defines the timeout value (in milliseconds) to be used when reading an
     * input stream from an URL connection. A timeout of zero is interpreted as
     * an infinite timeout. Default value is 2 minutes.
     */
    public static final IntProperty READ_TIMEOUT_PROPERTY =
            IntProperty.of("readTimeout", (int) TimeUnit.MINUTES.toMillis(2));

    /**
     * Defines the duration (in milliseconds) of response storage in the cache.
     * A duration of zero is interpreted as an infinite duration. Default value
     * is 5 minutes.
     */
    public static final LongProperty CACHE_TTL_PROPERTY =
            LongProperty.of("cacheTtl", TimeUnit.MINUTES.toMillis(5));

    /**
     * Defines the max number of redirects to be followed by HTTP client. This
     * limit is intended to prevent infinite loop. Default value is 5.
     */
    public static final IntProperty MAX_REDIRECTS_PROPERTY =
            IntProperty.of("maxRedirects", 5);

    /**
     * Defines if detail query is supported. Default value is false.
     */
    public static final BooleanProperty DETAIL_SUPPORTED_PROPERTY =
            BooleanProperty.of("detailSupported", false);

    /**
     * Defines if trailing slash is required in queries. Default value is false.
     */
    public static final BooleanProperty TRAILING_SLASH_REQUIRED_PROPERTY =
            BooleanProperty.of("trailingSlashRequired", false);

    /**
     * Defines if preemptive authentication should be used. Default value is false.
     */
    public static final BooleanProperty PREEMPTIVE_AUTHENTICATION_PROPERTY =
            BooleanProperty.of("preemptiveAuthentication", false);
}
