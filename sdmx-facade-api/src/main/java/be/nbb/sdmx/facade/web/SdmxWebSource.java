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
import org.checkerframework.checker.nullness.qual.NonNull;

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
    String description;

    @lombok.NonNull
    String driver;

    @lombok.NonNull
    URL endpoint;

    @lombok.Singular
    Map<String, String> properties;

    // Fix lombok.Builder.Default bug in NetBeans
    public static Builder builder() {
        return new Builder()
                .description("");
    }

    public static class Builder {

        @NonNull
        public Builder endpointOf(@NonNull String endpoint) throws IllegalArgumentException {
            try {
                return endpoint(new URL(endpoint));
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        @NonNull
        public Builder propertyOf(@NonNull String key, @NonNull Object value) throws IllegalArgumentException {
            return property(key, value.toString());
        }
    }
}
