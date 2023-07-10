/*
 * Copyright 2019 National Bank of Belgium
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
package _test.sdmxdl;

import lombok.NonNull;
import sdmxdl.Connection;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Philippe Charles
 */
public enum TestDriver implements WebDriver {
    VALID {
        @Override
        public @NonNull String getId() {
            return "valid";
        }

        @Override
        public int getRank() {
            return NATIVE_RANK;
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public @NonNull Connection connect(@NonNull SdmxWebSource source, @NonNull WebContext context) throws IllegalArgumentException {
            return TestConnection.TEST_VALID;
        }

        @Override
        public @NonNull Collection<SdmxWebSource> getDefaultSources() {
            return Collections.singletonList(SOURCE);
        }

        @Override
        public @NonNull Collection<String> getSupportedProperties() {
            return Collections.singletonList("hello");
        }
    }, FAILING {
        @Override
        public @NonNull String getId() {
            throw new CustomException();
        }

        @Override
        public int getRank() {
            throw new CustomException();
        }

        @Override
        public boolean isAvailable() {
            throw new CustomException();
        }

        @Override
        public @NonNull Connection connect(@NonNull SdmxWebSource source, @NonNull WebContext context) throws IllegalArgumentException {
            throw new CustomException();
        }

        @Override
        public @NonNull Collection<SdmxWebSource> getDefaultSources() {
            throw new CustomException();
        }

        @Override
        public @NonNull Collection<String> getSupportedProperties() {
            throw new CustomException();
        }
    }, NULL {
        @Override
        public @NonNull String getId() {
            return null;
        }

        @Override
        public int getRank() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isAvailable() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull Connection connect(@NonNull SdmxWebSource source, @NonNull WebContext context) throws IllegalArgumentException {
            return null;
        }

        @Override
        public @NonNull Collection<SdmxWebSource> getDefaultSources() {
            return null;
        }

        @Override
        public @NonNull Collection<String> getSupportedProperties() {
            return null;
        }
    };

    public static final SdmxWebSource SOURCE = SdmxWebSource.builder().id("123").driver("456").endpointOf("http://localhost").build();
}
