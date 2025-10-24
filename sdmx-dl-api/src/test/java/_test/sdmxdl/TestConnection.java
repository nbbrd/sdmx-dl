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
import nbbrd.design.NonNegative;
import sdmxdl.*;
import tests.sdmxdl.api.RepoSamples;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("DataFlowIssue")
public enum TestConnection implements Connection {
    TEST_VALID {
        @Override
        public @NonNull Optional<URI> testConnection() {
            return Optional.of(URI.create("http://localhost"));
        }

        @Override
        public @NonNull Collection<Database> getDatabases() {
            return RepoSamples.REPO.getDatabases();
        }

        @Override
        public @NonNull Collection<Flow> getFlows(@NonNull DatabaseRef database) {
            return RepoSamples.REPO.getFlows();
        }

        @Override
        public @NonNull MetaSet getMeta(@NonNull DatabaseRef database, @NonNull FlowRef flowRef) throws IOException, IllegalArgumentException {
            return RepoSamples.META_SET;
        }

        @Override
        public @NonNull DataSet getData(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) {
            return RepoSamples.DATA_SET;
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) {
            return RepoSamples.DATA_SET.getData().stream();
        }

        @Override
        public @NonNull Collection<String> getAvailableDimensionCodes(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Key constraints, @NonNegative int dimensionIndex) throws IOException, IllegalArgumentException {
            return RepoSamples.DATA_SET.getData().stream().map(Series::getKey).map(key -> key.get(dimensionIndex)).distinct().collect(toList());
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
        public @NonNull Optional<URI> testConnection() {
            throw new CustomException();
        }

        @Override
        public @NonNull Collection<Database> getDatabases() {
            throw new CustomException();
        }

        @Override
        public @NonNull Collection<Flow> getFlows(@NonNull DatabaseRef database) {
            throw new CustomException();
        }

        @Override
        public @NonNull MetaSet getMeta(@NonNull DatabaseRef database, @NonNull FlowRef flowRef) throws IOException, IllegalArgumentException {
            throw new CustomException();
        }

        @Override
        public @NonNull DataSet getData(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) {
            throw new CustomException();
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) {
            throw new CustomException();
        }

        @Override
        public @NonNull Collection<String> getAvailableDimensionCodes(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Key constraints, @NonNegative int dimensionIndex) throws IOException, IllegalArgumentException {
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
        public @NonNull Optional<URI> testConnection() {
            return null;
        }

        @Override
        public @NonNull Collection<Database> getDatabases() {
            return null;
        }

        @Override
        public @NonNull Collection<Flow> getFlows(@NonNull DatabaseRef database) {
            return null;
        }

        @Override
        public @NonNull MetaSet getMeta(@NonNull DatabaseRef database, @NonNull FlowRef flowRef) {
            return null;
        }

        @Override
        public @NonNull DataSet getData(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) {
            return null;
        }

        @Override
        public @NonNull Stream<Series> getDataStream(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) {
            return null;
        }

        @Override
        public @NonNull Collection<String> getAvailableDimensionCodes(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Key constraints, @NonNegative int dimensionIndex) throws IOException, IllegalArgumentException {
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
