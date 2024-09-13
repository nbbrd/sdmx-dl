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
import sdmxdl.Catalog;
import sdmxdl.Connection;
import sdmxdl.Languages;
import sdmxdl.Options;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Philippe Charles
 */
public enum TestDriver implements Driver {
    VALID {
        @Override
        public @NonNull String getDriverId() {
            return "VALID";
        }

        @Override
        public int getDriverRank() {
            return NATIVE_DRIVER_RANK;
        }

        @Override
        public boolean isDriverAvailable() {
            return true;
        }

        @Override
        public @NonNull Connection connect(@NonNull WebSource source, @NonNull Options options, @NonNull WebContext context) throws IllegalArgumentException {
            return TestConnection.TEST_VALID;
        }

        @Override
        public @NonNull List<Catalog> getCatalogs(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) throws IOException, IllegalArgumentException {
            return Collections.emptyList();
        }

        @Override
        public @NonNull Collection<WebSource> getDefaultSources() {
            return Collections.singletonList(SOURCE);
        }

        @Override
        public @NonNull Collection<String> getDriverProperties() {
            return Collections.singletonList("hello");
        }
    }, FAILING {
        @Override
        public @NonNull String getDriverId() {
            throw new CustomException();
        }

        @Override
        public int getDriverRank() {
            throw new CustomException();
        }

        @Override
        public boolean isDriverAvailable() {
            throw new CustomException();
        }

        @Override
        public @NonNull Connection connect(@NonNull WebSource source, @NonNull Options options, @NonNull WebContext context) throws IllegalArgumentException {
            throw new CustomException();
        }

        @Override
        public @NonNull List<Catalog> getCatalogs(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) throws IOException, IllegalArgumentException {
            throw new CustomException();
        }

        @Override
        public @NonNull Collection<WebSource> getDefaultSources() {
            throw new CustomException();
        }

        @Override
        public @NonNull Collection<String> getDriverProperties() {
            throw new CustomException();
        }
    }, NULL {
        @Override
        public @NonNull String getDriverId() {
            return null;
        }

        @Override
        public int getDriverRank() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDriverAvailable() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull Connection connect(@NonNull WebSource source, @NonNull Options options, @NonNull WebContext context) throws IllegalArgumentException {
            return null;
        }

        @Override
        public @NonNull List<Catalog> getCatalogs(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) throws IOException, IllegalArgumentException {
            return null;
        }

        @Override
        public @NonNull Collection<WebSource> getDefaultSources() {
            return null;
        }

        @Override
        public @NonNull Collection<String> getDriverProperties() {
            return null;
        }
    };

    public static final WebSource SOURCE = WebSource.builder().id("123").driver("456").endpointOf("http://localhost").build();
}
