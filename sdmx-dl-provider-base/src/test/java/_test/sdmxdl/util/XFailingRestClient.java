/*
 * Copyright 2017 National Bank of Belgium
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
package _test.sdmxdl.util;

import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.Marker;
import sdmxdl.provider.web.RestClient;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
public enum XFailingRestClient implements RestClient {

    TEST_EXPECTED {
        @Override
        public @NonNull Marker getMarker() {
            return Marker.parse("");
        }

        @Override
        public @NonNull List<Flow> getFlows() throws IOException {
            throw new CustomIOException();
        }

        @Override
        public @NonNull Flow getFlow(@NonNull FlowRef ref) throws IOException {
            throw new CustomIOException();
        }

        @Override
        public @NonNull Structure getStructure(@NonNull StructureRef ref) throws IOException {
            throw new CustomIOException();
        }

        @Override
        public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull Structure dsd) throws IOException {
            throw new CustomIOException();
        }

        @Override
        public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) throws IOException {
            throw new CustomIOException();
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() throws IOException {
            throw new CustomIOException();
        }

        @NonNull
        @Override
        public Optional<URI> testClient() throws IOException {
            throw new CustomIOException();
        }
    },
    TEST_UNEXPECTED {
        @Override
        public @NonNull Marker getMarker() {
            throw new CustomRuntimeException();
        }

        @Override
        public @NonNull List<Flow> getFlows() {
            throw new CustomRuntimeException();
        }

        @Override
        public @NonNull Flow getFlow(@NonNull FlowRef ref) {
            throw new CustomRuntimeException();
        }

        @Override
        public @NonNull Structure getStructure(@NonNull StructureRef ref) {
            throw new CustomRuntimeException();
        }

        @Override
        public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull Structure dsd) {
            throw new CustomRuntimeException();
        }

        @Override
        public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) {
            throw new CustomRuntimeException();
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() throws IOException {
            throw new CustomRuntimeException();
        }

        @NonNull
        @Override
        public Optional<URI> testClient() {
            throw new CustomRuntimeException();
        }
    },
    TEST_NULL {
        @Override
        public @NonNull Marker getMarker() {
            return null;
        }

        @Override
        public @NonNull List<Flow> getFlows() {
            return null;
        }

        @Override
        public @NonNull Flow getFlow(@NonNull FlowRef ref) {
            return null;
        }

        @Override
        public @NonNull Structure getStructure(@NonNull StructureRef ref) {
            return null;
        }

        @Override
        public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull Structure dsd) {
            return null;
        }

        @Override
        public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) {
            return null;
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() {
            return Collections.emptySet();
        }

        @NonNull
        @Override
        public Optional<URI> testClient() {
            return Optional.empty();
        }
    };

    private static final class CustomIOException extends IOException {
    }

    private static final class CustomRuntimeException extends RuntimeException {
    }
}
