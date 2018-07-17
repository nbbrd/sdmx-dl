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
package internal.web;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.util.UnexpectedIOException;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
final class FailsafeWebClient implements SdmxWebClient {

    @lombok.NonNull
    private final SdmxWebClient delegate;

    @lombok.NonNull
    private final Logger logger;

    @Override
    public String getName() throws IOException {
        String result;

        try {
            result = delegate.getName();
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while getting name");
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null while getting name");
        }

        return result;
    }

    @Override
    public List<Dataflow> getFlows() throws IOException {
        List<Dataflow> result;

        try {
            result = delegate.getFlows();
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while getting datasets with '%s'", getId());
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null while getting datasets with '%s'", getId());
        }

        return result;
    }

    @Override
    public Dataflow getFlow(DataflowRef ref) throws IOException {
        Dataflow result;

        try {
            result = delegate.getFlow(ref);
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while getting details for dataset '%s' with '%s'", ref, getId());
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null while getting details for dataset '%s' with '%s'", ref, getId());
        }

        return result;
    }

    @Override
    public DataStructure getStructure(DataStructureRef ref) throws IOException {
        DataStructure result;

        try {
            result = delegate.getStructure(ref);
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while getting data structure for dataset '%s' with '%s'", ref, getId());
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null while getting data structure for dataset '%s' with '%s'", ref, getId());
        }

        return result;
    }

    @Override
    public DataCursor getData(DataRequest request, DataStructure dsd) throws IOException {
        DataCursor result;

        try {
            result = delegate.getData(request, dsd);
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while getting data '%s' with '%s'", request, getId());
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null while getting data '%s' with '%s'", request, getId());
        }

        return result;
    }

    @Override
    public boolean isSeriesKeysOnlySupported() throws IOException {
        try {
            return delegate.isSeriesKeysOnlySupported();
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while checking keys-only support with '%s'", getId());
        }
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef flowRef) throws IOException {
        try {
            return delegate.peekStructureRef(flowRef);
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while peeking struct ref for dataset '%s' with '%s'", flowRef, getId());
        }
    }

    @Override
    public Duration ping() throws IOException {
        Duration result;

        try {
            result = delegate.ping();
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while pinging resource with '%s'", getId());
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null while pinging resource with '%s'", getId());
        }

        return result;
    }

    @SuppressWarnings("null")
    private String getId() {
        try {
            String result = delegate.getName();
            return result != null && !result.isEmpty() ? result : "NULL";
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    private IOException unexpected(RuntimeException ex, String format, Object... args) {
        logger.log(Level.WARNING, String.format(format, args), ex);
        return new UnexpectedIOException(ex);
    }

    private IOException unexpectedNull(String format, Object... args) {
        String msg = String.format(format, args);
        logger.log(Level.WARNING, msg);
        return new UnexpectedIOException(new NullPointerException(msg));
    }
}
