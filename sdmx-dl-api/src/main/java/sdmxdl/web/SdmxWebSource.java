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
import sdmxdl.LanguagePriorityList;
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
    String id;

    @lombok.Singular
    Map<String, String> names;

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
    public SdmxWebSource alias(@NonNull String id) throws IllegalArgumentException {
        if (!aliases.contains(id)) {
            throw new IllegalArgumentException(id);
        }
        return toBuilder().id(id).build();
    }

    public boolean isAlias() {
        return aliases.contains(id);
    }

    public @Nullable String getName(@NonNull LanguagePriorityList langs) {
        return langs.select(names);
    }

    public static final String ROOT_LANGUAGE = Locale.ROOT.getLanguage();

    public static class Builder {

        public @NonNull Builder nameOf(@NonNull CharSequence name) throws IllegalArgumentException {
            return name(ROOT_LANGUAGE, name.toString());
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
