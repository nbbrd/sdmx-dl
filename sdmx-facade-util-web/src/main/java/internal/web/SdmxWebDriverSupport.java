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
package internal.web;

import be.nbb.sdmx.facade.web.SdmxWebSource;
import be.nbb.sdmx.facade.web.SdmxWebConnection;
import be.nbb.sdmx.facade.web.spi.SdmxWebDriver;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import be.nbb.sdmx.facade.web.spi.SdmxWebContext;
import net.jcip.annotations.ThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.Builder(builderClassName = "Builder")
@ThreadSafe
public final class SdmxWebDriverSupport implements SdmxWebDriver {

    @lombok.Getter
    @lombok.NonNull
    private final String name;

    @lombok.Getter
    private final int rank;

    @lombok.NonNull
    private final SdmxWebClient.Supplier client;

    @lombok.Singular
    private final Collection<SdmxWebSource> sources;

    @lombok.Singular
    private final Collection<String> supportedProperties;

    // Fix lombok.Builder.Default bug in NetBeans
    public static Builder builder() {
        return new Builder()
                .rank(UNKNOWN);
    }

    @Override
    public SdmxWebConnection connect(SdmxWebSource source, SdmxWebContext context) throws IOException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(context);

        if (!source.getDriver().equals(name)) {
            throw new IllegalArgumentException(source.toString());
        }

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
    }
}
