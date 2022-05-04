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

import lombok.AccessLevel;
import lombok.NonNull;
import sdmxdl.Connection;
import sdmxdl.DataflowRef;
import sdmxdl.provider.Validator;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.IOException;
import java.util.Collection;

import static sdmxdl.provider.web.WebProperties.CACHE_TTL_PROPERTY;

/**
 * @author Philippe Charles
 */
@lombok.Builder(toBuilder = true)
public final class RestDriverSupport implements WebDriver {

    @lombok.Getter
    @lombok.NonNull
    private final String name;

    @lombok.Getter
    @lombok.Builder.Default
    private final int rank = UNKNOWN;

    @lombok.NonNull
    private final RestClientSupplier client;

    @lombok.Singular
    private final Collection<SdmxWebSource> sources;

    @lombok.Singular
    private final Collection<String> supportedProperties;

    @lombok.Getter
    @lombok.Builder.Default
    private final String defaultDialect = NO_DEFAULT_DIALECT;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Validator<DataflowRef> dataflowRefValidator = WebValidators.DEFAULT_DATAFLOW_REF_VALIDATOR;

    @lombok.Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final Validator<SdmxWebSource> sourceValidator = WebValidators.onDriverName(name);

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public @NonNull Connection connect(@NonNull SdmxWebSource source, @NonNull WebContext context) throws IOException {
        getSourceValidator().checkValidity(source);

        return RestConnection.of(getClient(source, context), dataflowRefValidator, false);
    }

    @Override
    public @NonNull Collection<SdmxWebSource> getDefaultSources() {
        return sources;
    }

    @Override
    public @NonNull Collection<String> getSupportedProperties() {
        return supportedProperties;
    }

    private RestClient getClient(SdmxWebSource source, WebContext context) throws IOException {
        return CachedRestClient.of(
                client.get(source, context),
                context.getCache(),
                CACHE_TTL_PROPERTY.get(source.getProperties()),
                source,
                context.getLanguages());
    }

    public static final class Builder {

        @NonNull
        public Builder supportedPropertyOf(@NonNull CharSequence property) {
            return supportedProperty(property.toString());
        }
    }
}
