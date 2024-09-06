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
package sdmxdl.provider.web;

import nbbrd.io.text.*;
import sdmxdl.About;
import sdmxdl.format.design.PropertyDefinition;

import java.util.concurrent.TimeUnit;

import static sdmxdl.web.spi.Driver.DRIVER_PROPERTY_PREFIX;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class DriverProperties {

    /**
     * Defines the timeout value (in milliseconds) to be used when opening a
     * URL connection. A timeout of zero is interpreted as an infinite timeout.
     * Default value is 2 minutes.
     */
    @PropertyDefinition
    public static final IntProperty CONNECT_TIMEOUT_PROPERTY =
            IntProperty.of(DRIVER_PROPERTY_PREFIX + ".connectTimeout", (int) TimeUnit.MINUTES.toMillis(2));

    /**
     * Defines the timeout value (in milliseconds) to be used when reading an
     * input stream from a URL connection. A timeout of zero is interpreted as
     * an infinite timeout. Default value is 2 minutes.
     */
    @PropertyDefinition
    public static final IntProperty READ_TIMEOUT_PROPERTY =
            IntProperty.of(DRIVER_PROPERTY_PREFIX + ".readTimeout", (int) TimeUnit.MINUTES.toMillis(2));

    /**
     * Defines the duration (in milliseconds) of response storage in the cache.
     * A duration of zero is interpreted as an infinite duration. Default value
     * is 5 minutes.
     */
    @PropertyDefinition
    public static final LongProperty CACHE_TTL_PROPERTY =
            LongProperty.of(DRIVER_PROPERTY_PREFIX + ".cacheTtl", TimeUnit.MINUTES.toMillis(5));

    /**
     * Defines the max number of redirects to be followed by HTTP client. This
     * limit is intended to prevent infinite loop. Default value is 5.
     */
    @PropertyDefinition
    public static final IntProperty MAX_REDIRECTS_PROPERTY =
            IntProperty.of(DRIVER_PROPERTY_PREFIX + ".maxRedirects", 5);

    /**
     * Defines if detail query is supported. Default value is false.
     */
    @PropertyDefinition
    public static final BooleanProperty DETAIL_SUPPORTED_PROPERTY =
            BooleanProperty.of(DRIVER_PROPERTY_PREFIX + ".detailSupported", false);

    /**
     * Defines if trailing slash is required in queries. Default value is false.
     */
    @PropertyDefinition
    public static final BooleanProperty TRAILING_SLASH_PROPERTY =
            BooleanProperty.of(DRIVER_PROPERTY_PREFIX + ".trailingSlash", false);

    /**
     * Defines if preemptive authentication should be used. Default value is false.
     */
    @PropertyDefinition
    public static final BooleanProperty PREEMPTIVE_AUTH_PROPERTY =
            BooleanProperty.of(DRIVER_PROPERTY_PREFIX + ".preemptiveAuth", false);

    /**
     * Defines the user-agent request header. Default value is library name and version.
     */
    @PropertyDefinition
    public static final Property<String> USER_AGENT_PROPERTY =
            Property.of(DRIVER_PROPERTY_PREFIX + ".userAgent", About.NAME + "/" + About.VERSION, Parser.onString(), Formatter.onString());
}
