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

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.SdmxSource;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.EqualsAndHashCode(callSuper = false)
public class SdmxWebSource extends SdmxSource {

    @lombok.NonNull
    String name;

    @lombok.Singular
    Map<String, String> descriptions;

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
    URI monitor;

    @Nullable
    @lombok.Builder.Default
    URL monitorWebsite = null;

    @NonNull
    public SdmxWebSource alias(@NonNull String name) throws IllegalArgumentException {
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

        public @NonNull Builder descriptionOf(@NonNull CharSequence description) throws IllegalArgumentException {
            return description(Locale.ROOT.getLanguage(), description.toString());
        }

        public @NonNull Builder endpointOf(@NonNull String endpoint) throws IllegalArgumentException {
            return endpoint(URI.create(endpoint));
        }

        public @NonNull Builder propertyOf(@NonNull CharSequence key, @NonNull Object value) {
            return property(key.toString(), value.toString());
        }

        public @NonNull Builder websiteOf(@Nullable String website) throws IllegalArgumentException {
            try {
                return website(new URL(website));
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        public @NonNull Builder monitorOf(@NonNull String monitor) {
            return monitor(URI.create(monitor));
        }

        public @NonNull Builder monitorWebsiteOf(@NonNull String monitorWebsite) throws IllegalArgumentException {
            try {
                return monitorWebsite(new URL(monitorWebsite));
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
    }
}
