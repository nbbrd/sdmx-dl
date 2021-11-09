/*
 * Copyright 2015 National Bank of Belgium
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
package sdmxdl.web;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class SdmxWebSource {

    @lombok.NonNull
    String name;

    @Nullable
    String description;

    @lombok.NonNull
    String driver;

    @Nullable
    String dialect;

    @lombok.NonNull
    URI endpoint;

    @lombok.Singular
    Map<String, String> properties;

    @lombok.Singular
    Set<String> aliases;

    @Nullable
    URL website;

    @Nullable
    SdmxWebMonitor monitor;

    @NonNull
    public SdmxWebSource alias(@NonNull String name) throws IllegalArgumentException {
        Objects.requireNonNull(name);
        if (!aliases.contains(name)) {
            throw new IllegalArgumentException(name);
        }
        return toBuilder().name(name).build();
    }

    public boolean isAlias() {
        return aliases.contains(name);
    }

    public @NonNull String getId() {
        return getDriver() + ":" + getName();
    }

    public static class Builder {

        @NonNull
        public Builder endpointOf(@NonNull String endpoint) throws IllegalArgumentException {
            Objects.requireNonNull(endpoint);
            return endpoint(URI.create(endpoint));
        }

        @NonNull
        public Builder propertyOf(@NonNull CharSequence key, @NonNull Object value) {
            Objects.requireNonNull(key);
            return property(key.toString(), value.toString());
        }

        @NonNull
        public Builder websiteOf(@Nullable String website) throws IllegalArgumentException {
            Objects.requireNonNull(website);
            try {
                return website(new URL(website));
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        @NonNull
        public Builder monitorOf(@NonNull String provider, @NonNull String id) {
            return monitor(SdmxWebMonitor.builder().provider(provider).id(id).build());
        }
    }
}
