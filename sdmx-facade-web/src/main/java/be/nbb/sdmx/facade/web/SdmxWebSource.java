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
package be.nbb.sdmx.facade.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public final class SdmxWebSource {

    @lombok.NonNull
    String name;

    @lombok.NonNull
    @lombok.Builder.Default
    String description = "";

    @lombok.NonNull
    String driver;

    @lombok.NonNull
    URL endpoint;

    @lombok.Singular
    Map<String, String> properties;

    public static class Builder {

        @Nonnull
        public Builder endpointOf(@Nonnull String endpoint) throws IllegalArgumentException {
            try {
                return endpoint(new URL(endpoint));
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        @Nonnull
        public Builder propertyOf(@Nonnull String key, @Nonnull Object value) throws IllegalArgumentException {
            return property(key, value.toString());
        }
    }
}
