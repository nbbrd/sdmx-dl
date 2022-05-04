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
import sdmxdl.provider.web.SdmxRestClient;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
public enum XFailingRestClient implements SdmxRestClient {

    EXPECTED {
        @Override
        public @NonNull String getName() {
            return "";
        }

        @Override
        public @NonNull List<Dataflow> getFlows() throws IOException {
            throw new CustomIOException();
        }

        @Override
        public @NonNull Dataflow getFlow(@NonNull DataflowRef ref) throws IOException {
            throw new CustomIOException();
        }

        @Override
        public @NonNull DataStructure getStructure(@NonNull DataStructureRef ref) throws IOException {
            throw new CustomIOException();
        }

        @Override
        public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull DataStructure dsd) throws IOException {
            throw new CustomIOException();
        }

        @Override
        public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) throws IOException {
            throw new CustomIOException();
        }

        @Override
        public boolean isDetailSupported() throws IOException {
            throw new CustomIOException();
        }

        @Override
        public DataStructureRef peekStructureRef(@NonNull DataflowRef flowRef) throws IOException {
            throw new CustomIOException();
        }

        @Override
        public void testClient() throws IOException {
            throw new CustomIOException();
        }
    },
    UNEXPECTED {
        @Override
        public @NonNull String getName() {
            throw new CustomRuntimeException();
        }

        @Override
        public @NonNull List<Dataflow> getFlows() {
            throw new CustomRuntimeException();
        }

        @Override
        public @NonNull Dataflow getFlow(@NonNull DataflowRef ref) {
            throw new CustomRuntimeException();
        }

        @Override
        public @NonNull DataStructure getStructure(@NonNull DataStructureRef ref) {
            throw new CustomRuntimeException();
        }

        @Override
        public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull DataStructure dsd) {
            throw new CustomRuntimeException();
        }

        @Override
        public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) {
            throw new CustomRuntimeException();
        }

        @Override
        public boolean isDetailSupported() {
            throw new CustomRuntimeException();
        }

        @Override
        public DataStructureRef peekStructureRef(@NonNull DataflowRef flowRef) {
            throw new CustomRuntimeException();
        }

        @Override
        public void testClient() {
            throw new CustomRuntimeException();
        }
    },
    NULL {
        @Override
        public @NonNull String getName() {
            return null;
        }

        @Override
        public @NonNull List<Dataflow> getFlows() {
            return null;
        }

        @Override
        public @NonNull Dataflow getFlow(@NonNull DataflowRef ref) {
            return null;
        }

        @Override
        public @NonNull DataStructure getStructure(@NonNull DataStructureRef ref) {
            return null;
        }

        @Override
        public @NonNull Stream<Series> getData(@NonNull DataRef ref, @NonNull DataStructure dsd) {
            return null;
        }

        @Override
        public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) {
            return null;
        }

        @Override
        public boolean isDetailSupported() {
            return false;
        }

        @Override
        public DataStructureRef peekStructureRef(@NonNull DataflowRef flowRef) {
            return null;
        }

        @Override
        public void testClient() {
        }
    };

    private static final class CustomIOException extends IOException {
    }

    private static final class CustomRuntimeException extends RuntimeException {
    }
}
