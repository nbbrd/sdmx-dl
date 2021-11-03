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

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.util.web.DataRef;
import sdmxdl.util.web.SdmxRestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * @author Philippe Charles
 */
public enum XFailingRestClient implements SdmxRestClient {

    EXPECTED {
        @Override
        public String getName() throws IOException {
            throw new CustomIOException();
        }

        @Override
        public List<Dataflow> getFlows() throws IOException {
            throw new CustomIOException();
        }

        @Override
        public Dataflow getFlow(DataflowRef ref) throws IOException {
            throw new CustomIOException();
        }

        @Override
        public DataStructure getStructure(DataStructureRef ref) throws IOException {
            throw new CustomIOException();
        }

        @Override
        public DataCursor getData(DataRef ref, DataStructure dsd) throws IOException {
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
        public DataStructureRef peekStructureRef(DataflowRef flowRef) throws IOException {
            throw new CustomIOException();
        }

        @Override
        public Duration ping() throws IOException {
            throw new CustomIOException();
        }
    },
    UNEXPECTED {
        @Override
        public String getName() {
            throw new CustomRuntimeException();
        }

        @Override
        public List<Dataflow> getFlows() {
            throw new CustomRuntimeException();
        }

        @Override
        public Dataflow getFlow(DataflowRef ref) {
            throw new CustomRuntimeException();
        }

        @Override
        public DataStructure getStructure(DataStructureRef ref) {
            throw new CustomRuntimeException();
        }

        @Override
        public DataCursor getData(DataRef ref, DataStructure dsd) {
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
        public DataStructureRef peekStructureRef(DataflowRef flowRef) {
            throw new CustomRuntimeException();
        }

        @Override
        public Duration ping() {
            throw new CustomRuntimeException();
        }
    },
    NULL {
        @Override
        public String getName() {
            return null;
        }

        @Override
        public List<Dataflow> getFlows() {
            return null;
        }

        @Override
        public Dataflow getFlow(DataflowRef ref) {
            Objects.requireNonNull(ref);
            return null;
        }

        @Override
        public DataStructure getStructure(DataStructureRef ref) {
            Objects.requireNonNull(ref);
            return null;
        }

        @Override
        public DataCursor getData(DataRef ref, DataStructure dsd) {
            Objects.requireNonNull(ref);
            Objects.requireNonNull(dsd);
            return null;
        }

        @Override
        public @NonNull Codelist getCodelist(@NonNull CodelistRef ref) {
            Objects.requireNonNull(ref);
            return null;
        }

        @Override
        public boolean isDetailSupported() {
            return false;
        }

        @Override
        public DataStructureRef peekStructureRef(DataflowRef flowRef) {
            Objects.requireNonNull(flowRef);
            return null;
        }

        @Override
        public Duration ping() {
            return null;
        }
    };

    private static final class CustomIOException extends IOException {
    }

    private static final class CustomRuntimeException extends RuntimeException {
    }
}
