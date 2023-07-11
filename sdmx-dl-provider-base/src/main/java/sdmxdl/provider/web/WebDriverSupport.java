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
import sdmxdl.LanguagePriorityList;
import sdmxdl.provider.Validator;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.function.Predicate;

/**
 * @author Philippe Charles
 */
@lombok.Builder(toBuilder = true)
public final class WebDriverSupport implements WebDriver {

    @lombok.Getter
    @NonNull
    private final String id;

    @lombok.Getter
    @lombok.Builder.Default
    private final int rank = UNKNOWN_RANK;

    @NonNull
    private final WebConnector connector;

    @lombok.Singular
    private final Collection<SdmxWebSource> sources;

    @lombok.Singular
    private final Collection<String> supportedProperties;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Predicate<Properties> availability = properties -> true;

    @lombok.Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final Validator<SdmxWebSource> lazySourceValidator = WebValidators.onDriverId(id);

    @Override
    public boolean isAvailable() {
        return availability.test(System.getProperties());
    }

    @Override
    public @NonNull Connection connect(@NonNull SdmxWebSource source, @NonNull LanguagePriorityList languages, @NonNull WebContext context) throws IOException {
        getLazySourceValidator().checkValidity(source);

        return connector.connect(source, languages, context);
    }

    @Override
    public @NonNull Collection<SdmxWebSource> getDefaultSources() {
        return sources;
    }

    @Override
    public @NonNull Collection<String> getSupportedProperties() {
        return supportedProperties;
    }

    public static final class Builder {

        @NonNull
        public Builder supportedPropertyOf(@NonNull CharSequence property) {
            return supportedProperty(property.toString());
        }
    }
}
