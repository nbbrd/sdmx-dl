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

import sdmxdl.DataCursor;
import sdmxdl.DataFilter;
import sdmxdl.DataStructure;
import sdmxdl.DataStructureRef;
import sdmxdl.Dataflow;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.Series;
import sdmxdl.web.SdmxWebConnection;
import java.io.IOException;
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
        public Duration ping() throws IOException {
            return PING;
        }

        @Override
        public String getDriver() throws IOException {
            return DRIVER;
        }

        @Override
        public Collection<Dataflow> getFlows() throws IOException {
            return FLOWS;
        }

        @Override
        public Dataflow getFlow(DataflowRef flowRef) throws IOException {
            return FLOW;
        }

        @Override
        public DataStructure getStructure(DataflowRef flowRef) throws IOException {
            return STRUCT;
        }

        @Override
        public List<Series> getData(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
            return DATA;
        }

        @Override
        public Stream<Series> getDataStream(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
            return DATA.stream();
        }

        @Override
        public DataCursor getDataCursor(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
            return DataCursor.empty();
        }

        @Override
        public boolean isSeriesKeysOnlySupported() throws IOException {
            return true;
        }

        @Override
        public void close() throws IOException {
        }
    },
    FAILING {
        @Override
        public Duration ping() throws IOException {
            throw new CustomException();
        }

        @Override
        public String getDriver() throws IOException {
            throw new CustomException();
        }

        @Override
        public Collection<Dataflow> getFlows() throws IOException {
            throw new CustomException();
        }

        @Override
        public Dataflow getFlow(DataflowRef flowRef) throws IOException {
            throw new CustomException();
        }

        @Override
        public DataStructure getStructure(DataflowRef flowRef) throws IOException {
            throw new CustomException();
        }

        @Override
        public List<Series> getData(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
            throw new CustomException();
        }

        @Override
        public Stream<Series> getDataStream(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
            throw new CustomException();
        }

        @Override
        public DataCursor getDataCursor(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
            throw new CustomException();
        }

        @Override
        public boolean isSeriesKeysOnlySupported() throws IOException {
            throw new CustomException();
        }

        @Override
        public void close() throws IOException {
            throw new CustomException();
        }
    },
    NULL {
        @Override
        public Duration ping() throws IOException {
            return null;
        }

        @Override
        public String getDriver() throws IOException {
            return null;
        }

        @Override
        public Collection<Dataflow> getFlows() throws IOException {
            return null;
        }

        @Override
        public Dataflow getFlow(DataflowRef flowRef) throws IOException {
            return null;
        }

        @Override
        public DataStructure getStructure(DataflowRef flowRef) throws IOException {
            return null;
        }

        @Override
        public List<Series> getData(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
            return null;
        }

        @Override
        public Stream<Series> getDataStream(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
            return null;
        }

        @Override
        public DataCursor getDataCursor(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
            return null;
        }

        @Override
        public boolean isSeriesKeysOnlySupported() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() throws IOException {
            throw new UnsupportedOperationException();
        }
    };

    public static final Duration PING = Duration.ofMillis(123);
    public static final String DRIVER = "validDriver";
    public static final Collection<Dataflow> FLOWS = Collections.emptyList();
    public static final DataflowRef FLOW_REF = DataflowRef.parse("flow");
    public static final DataStructureRef STRUCT_REF = DataStructureRef.parse("struct");
    public static final Dataflow FLOW = Dataflow.of(FLOW_REF, STRUCT_REF, "label");
    public static final DataStructure STRUCT = DataStructure.builder().ref(STRUCT_REF).label("").build();
    public static final List<Series> DATA = Collections.emptyList();
    public static final Key KEY = Key.ALL;
    public static final DataFilter FILTER = DataFilter.ALL;
}
