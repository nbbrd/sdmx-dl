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

import sdmxdl.*;
import sdmxdl.web.SdmxWebConnection;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
public enum TestConnection implements SdmxWebConnection {
    VALID {

        @Override
        public Duration ping() {
            return PING;
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
        public List<Series> getData(DataflowRef flowRef, Key key, DataFilter filter) {
            return DATA;
        }

        @Override
        public Stream<Series> getDataStream(DataflowRef flowRef, Key key, DataFilter filter) {
            return DATA.stream();
        }

        @Override
        public DataCursor getDataCursor(DataflowRef flowRef, Key key, DataFilter filter) {
            return DataCursor.empty();
        }

        @Override
        public boolean isSeriesKeysOnlySupported() {
            return true;
        }

        @Override
        public void close() {
        }
    },
    FAILING {
        @Override
        public Duration ping() {
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
        public List<Series> getData(DataflowRef flowRef, Key key, DataFilter filter) {
            throw new CustomException();
        }

        @Override
        public Stream<Series> getDataStream(DataflowRef flowRef, Key key, DataFilter filter) {
            throw new CustomException();
        }

        @Override
        public DataCursor getDataCursor(DataflowRef flowRef, Key key, DataFilter filter) {
            throw new CustomException();
        }

        @Override
        public boolean isSeriesKeysOnlySupported() {
            throw new CustomException();
        }

        @Override
        public void close() {
            throw new CustomException();
        }
    },
    NULL {
        @Override
        public Duration ping() {
            return null;
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
        public List<Series> getData(DataflowRef flowRef, Key key, DataFilter filter) {
            return null;
        }

        @Override
        public Stream<Series> getDataStream(DataflowRef flowRef, Key key, DataFilter filter) {
            return null;
        }

        @Override
        public DataCursor getDataCursor(DataflowRef flowRef, Key key, DataFilter filter) {
            return null;
        }

        @Override
        public boolean isSeriesKeysOnlySupported() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException();
        }
    };

    public static final Duration PING = Duration.ofMillis(123);
    public static final String DRIVER = "validDriver";
    public static final Collection<Dataflow> FLOWS = Collections.emptyList();
    public static final DataflowRef FLOW_REF = DataflowRef.parse("flow");
    public static final DataStructureRef STRUCT_REF = DataStructureRef.parse("struct");
    public static final Dataflow FLOW = Dataflow.of(FLOW_REF, STRUCT_REF, "label");
    public static final DataStructure STRUCT = DataStructure.builder().ref(STRUCT_REF).primaryMeasureId("").label("").build();
    public static final List<Series> DATA = Collections.emptyList();
    public static final Key KEY = Key.ALL;
    public static final DataFilter FILTER = DataFilter.ALL;
}
