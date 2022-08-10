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
import sdmxdl.*;
import tests.sdmxdl.api.RepoSamples;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
public enum TestConnection implements Connection {
    TEST_VALID {
        @Override
        public void testConnection() {
        }

        @Override
        public @NonNull Collection<Dataflow> getFlows() {
            return RepoSamples.REPO.getFlows();
        }

        @Override
        public @NonNull Dataflow getFlow(@NonNull DataflowRef flowRef) {
            return RepoSamples.FLOW;
        }

        @Override
        public @NonNull DataStructure getStructure(@NonNull DataflowRef flowRef) {
            return RepoSamples.STRUCT;
        }

        @Override
        public @NonNull DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) {
            return RepoSamples.DATA_SET;
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) {
            return RepoSamples.DATA_SET.getData().stream();
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() {
            return EnumSet.noneOf(Feature.class);
        }

        @Override
        public void close() {
        }
    },
    TEST_FAILING {
        @Override
        public void testConnection() {
            throw new CustomException();
        }

        @Override
        public @NonNull Collection<Dataflow> getFlows() {
            throw new CustomException();
        }

        @Override
        public @NonNull Dataflow getFlow(@NonNull DataflowRef flowRef) {
            throw new CustomException();
        }

        @Override
        public @NonNull DataStructure getStructure(@NonNull DataflowRef flowRef) {
            throw new CustomException();
        }

        @Override
        public @NonNull DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) {
            throw new CustomException();
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) {
            throw new CustomException();
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() {
            throw new CustomException();
        }

        @Override
        public void close() {
            throw new CustomException();
        }
    },
    TEST_NULL {
        @Override
        public void testConnection() {
        }

        @Override
        public @NonNull Collection<Dataflow> getFlows() {
            return null;
        }

        @Override
        public @NonNull Dataflow getFlow(@NonNull DataflowRef flowRef) {
            return null;
        }

        @Override
        public @NonNull DataStructure getStructure(@NonNull DataflowRef flowRef) {
            return null;
        }

        @Override
        public @NonNull DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) {
            return null;
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) {
            return null;
        }

        @Override
        public @NonNull Set<Feature> getSupportedFeatures() {
            return null;
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException();
        }
    }
}
