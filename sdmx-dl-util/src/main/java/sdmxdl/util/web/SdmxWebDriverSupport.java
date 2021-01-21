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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.web.SdmxWebConnection;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

/**
 * @author Philippe Charles
 */
@lombok.Builder(toBuilder = true)
public final class SdmxWebDriverSupport implements SdmxWebDriver {

    @lombok.Getter
    @lombok.NonNull
    private final String name;

    @lombok.Getter
    @lombok.Builder.Default
    private final int rank = UNKNOWN;

    @lombok.NonNull
    private final SdmxWebClient.Supplier client;

    @lombok.Singular
    private final Collection<SdmxWebSource> sources;

    @lombok.Singular
    private final Collection<String> supportedProperties;

    @Override
    public SdmxWebConnection connect(SdmxWebSource source, SdmxWebContext context) throws IOException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(context);
        checkSource(source, name);

        return SdmxWebConnectionImpl.of(getClient(source, context), name);
    }

    @Override
    public Collection<SdmxWebSource> getDefaultSources() {
        return sources;
    }

    @Override
    public Collection<String> getSupportedProperties() {
        return supportedProperties;
    }

    private SdmxWebClient getClient(SdmxWebSource source, SdmxWebContext context) throws IOException {
        return CachedWebClient.of(
                client.get(source, context),
                context.getCache(),
                SdmxWebProperty.getCacheTtl(source.getProperties()),
                source,
                context.getLanguages());
    }

    public static final class Builder {

        @NonNull
        public Builder sourceOf(@NonNull String name, @NonNull String description, @NonNull String endpoint) {
            return source(SdmxWebSource.builder().name(name).description(description).driver(this.name).endpointOf(endpoint).build());
        }

        @NonNull
        public Builder sourceOf(@NonNull String name, @NonNull String description, @NonNull String endpoint, @Nullable String website) {
            return source(SdmxWebSource.builder().name(name).description(description).driver(this.name).endpointOf(endpoint).websiteOf(website).build());
        }
    }

    public static void checkSource(@NonNull SdmxWebSource source, @NonNull String name) throws IllegalArgumentException {
        if (!source.getDriver().equals(name)) {
            throw new IllegalArgumentException(source.toString());
        }
    }
}
