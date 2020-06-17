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
package _test.client;

import be.nbb.sdmx.facade.*;
import internal.web.DataRequest;
import internal.web.SdmxWebClient;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Philippe Charles
 */
public enum XFailingWebClient implements SdmxWebClient {

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
        public DataCursor getData(DataRequest request, DataStructure dsd) throws IOException {
            throw new CustomIOException();
        }

        @Override
        public boolean isSeriesKeysOnlySupported() throws IOException {
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
        public String getName() throws IOException {
            throw new CustomRuntimeException();
        }

        @Override
        public List<Dataflow> getFlows() throws IOException {
            throw new CustomRuntimeException();
        }

        @Override
        public Dataflow getFlow(DataflowRef ref) throws IOException {
            throw new CustomRuntimeException();
        }

        @Override
        public DataStructure getStructure(DataStructureRef ref) throws IOException {
            throw new CustomRuntimeException();
        }

        @Override
        public DataCursor getData(DataRequest request, DataStructure dsd) throws IOException {
            throw new CustomRuntimeException();
        }

        @Override
        public boolean isSeriesKeysOnlySupported() throws IOException {
            throw new CustomRuntimeException();
        }

        @Override
        public DataStructureRef peekStructureRef(DataflowRef flowRef) throws IOException {
            throw new CustomRuntimeException();
        }

        @Override
        public Duration ping() throws IOException {
            throw new CustomRuntimeException();
        }
    },
    NULL {
        @Override
        public String getName() throws IOException {
            return null;
        }

        @Override
        public List<Dataflow> getFlows() throws IOException {
            return null;
        }

        @Override
        public Dataflow getFlow(DataflowRef ref) throws IOException {
            Objects.requireNonNull(ref);
            return null;
        }

        @Override
        public DataStructure getStructure(DataStructureRef ref) throws IOException {
            Objects.requireNonNull(ref);
            return null;
        }

        @Override
        public DataCursor getData(DataRequest request, DataStructure dsd) throws IOException {
            Objects.requireNonNull(request);
            Objects.requireNonNull(dsd);
            return null;
        }

        @Override
        public boolean isSeriesKeysOnlySupported() throws IOException {
            return false;
        }

        @Override
        public DataStructureRef peekStructureRef(DataflowRef flowRef) throws IOException {
            Objects.requireNonNull(flowRef);
            return null;
        }

        @Override
        public Duration ping() throws IOException {
            return null;
        }
    };

    private static final class CustomIOException extends IOException {
    }

    private static final class CustomRuntimeException extends RuntimeException {
    }
}
