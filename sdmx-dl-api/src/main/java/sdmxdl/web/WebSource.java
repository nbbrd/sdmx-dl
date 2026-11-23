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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.Confidentiality;
import sdmxdl.Languages;
import sdmxdl.Source;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.EqualsAndHashCode(callSuper = false)
public class WebSource extends Source {

    @NonNull String id;

    @lombok.Singular
    @NonNull Map<String, String> names;

    @NonNull String driver;

    @NonNull URI endpoint;

    @lombok.Singular
    @NonNull Map<String, String> properties;

    @lombok.Singular
    @NonNull Set<String> aliases;

    @lombok.Builder.Default
    @NonNull Confidentiality confidentiality = Confidentiality.RESTRICTED;

    @Nullable URL website;

    @Nullable URI monitor;

    @Nullable URL monitorWebsite;

    /**
     * The icon of this source: either a bundled icon ({@code sdmx-dl:icon/<name>})
     * or an inline icon ({@code data:image/png;base64,...} or {@code data:image/svg+xml,...}).
     *
     * @see SdmxWebManager#getIcons(WebSource)
     */
    // excluded from toString to keep unique ids (cache keys, credentials) stable and to avoid logging large data URIs
    @lombok.ToString.Exclude
    @Nullable URI icon;

    public @NonNull WebSource alias(@NonNull String id) throws IllegalArgumentException {
        if (!aliases.contains(id)) {
            throw new IllegalArgumentException(id);
        }
        return toBuilder().id(id).build();
    }

    public boolean isAlias() {
        return aliases.contains(id);
    }

    public @Nullable String getName(@NonNull Languages languages) {
        return languages.select(names);
    }

    public static class Builder {

        public @NonNull Builder endpointOf(@NonNull CharSequence endpoint) throws IllegalArgumentException {
            return endpoint(URI.create(endpoint.toString()));
        }

        public @NonNull Builder propertyOf(@NonNull CharSequence key, @NonNull Object value) {
            return property(key.toString(), value.toString());
        }

        public @NonNull Builder websiteOf(@Nullable CharSequence website) throws IllegalArgumentException {
            try {
                return website(website != null ? URI.create(website.toString()).toURL() : null);
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        public @NonNull Builder monitorOf(@Nullable CharSequence monitor) {
            return monitor(monitor != null ? URI.create(monitor.toString()) : null);
        }

        public @NonNull Builder monitorWebsiteOf(@Nullable CharSequence monitorWebsite)
                throws IllegalArgumentException {
            try {
                return monitorWebsite(
                        monitorWebsite != null
                                ? URI.create(monitorWebsite.toString()).toURL()
                                : null);
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        public @NonNull Builder iconOf(@Nullable CharSequence icon) throws IllegalArgumentException {
            return icon(icon != null ? URI.create(icon.toString()) : null);
        }
    }
}
