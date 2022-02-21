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

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.web.SdmxWebConnection;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
public enum TestConnection implements SdmxWebConnection {
    VALID {
        @Override
        public void testConnection() {
        }

        @Override
        public String getDriver() {
            return DRIVER;
        }

        @Override
        public Collection<Dataflow> getFlows() {
            return FLOWS;
        }

        @Override
        public Dataflow getFlow(DataflowRef flowRef) {
            return FLOW;
        }

        @Override
        public DataStructure getStructure(DataflowRef flowRef) {
            return STRUCT;
        }

        @Override
        public DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) {
            return DATA;
        }

        @Override
        public Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) {
            return DATA.getData().stream();
        }

        @Override
        public Set<Feature> getSupportedFeatures() {
            return EnumSet.noneOf(Feature.class);
        }

        @Override
        public void close() {
        }
    },
    FAILING {
        @Override
        public void testConnection() {
            throw new CustomException();
        }

        @Override
        public String getDriver() {
            throw new CustomException();
        }

        @Override
        public Collection<Dataflow> getFlows() {
            throw new CustomException();
        }

        @Override
        public Dataflow getFlow(DataflowRef flowRef) {
            throw new CustomException();
        }

        @Override
        public DataStructure getStructure(DataflowRef flowRef) {
            throw new CustomException();
        }

        @Override
        public DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) {
            throw new CustomException();
        }

        @Override
        public Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) {
            throw new CustomException();
        }

        @Override
        public Set<Feature> getSupportedFeatures() {
            throw new CustomException();
        }

        @Override
        public void close() {
            throw new CustomException();
        }
    },
    NULL {
        @Override
        public void testConnection() {
        }

        @Override
        public String getDriver() {
            return null;
        }

        @Override
        public Collection<Dataflow> getFlows() {
            return null;
        }

        @Override
        public Dataflow getFlow(DataflowRef flowRef) {
            return null;
        }

        @Override
        public DataStructure getStructure(DataflowRef flowRef) {
            return null;
        }

        @Override
        public DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) {
            return null;
        }

        @Override
        public Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) {
            return null;
        }

        @Override
        public Set<Feature> getSupportedFeatures() {
            return null;
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException();
        }
    };

    public static final String DRIVER = "validDriver";
    public static final Collection<Dataflow> FLOWS = Collections.emptyList();
    public static final DataflowRef FLOW_REF = DataflowRef.parse("flow");
    public static final DataStructureRef STRUCT_REF = DataStructureRef.parse("struct");
    public static final Dataflow FLOW = Dataflow.of(FLOW_REF, STRUCT_REF, "label");
    public static final DataStructure STRUCT = DataStructure.builder().ref(STRUCT_REF).primaryMeasureId("").label("").build();
    public static final DataSet DATA = DataSet.builder().ref(FLOW_REF).build();
    public static final Key KEY = Key.ALL;
    public static final DataDetail FILTER = DataDetail.FULL;
}
