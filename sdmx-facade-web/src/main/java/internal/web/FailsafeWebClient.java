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
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.util.UnexpectedIOException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
@lombok.extern.java.Log
final class FailsafeWebClient implements WebClient {

    @lombok.NonNull
    private final WebClient delegate;

    @Override
    public List<Dataflow> getFlows() throws IOException {
        List<Dataflow> result;

        try {
            result = delegate.getFlows();
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while getting datasets");
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null while getting datasets");
        }

        return result;
    }

    @Override
    public Dataflow getFlow(DataflowRef ref) throws IOException {
        Dataflow result;

        try {
            result = delegate.getFlow(ref);
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while getting details from dataset '%s'", ref);
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null while getting details from dataset '%s'", ref);
        }

        return result;
    }

    @Override
    public DataStructure getStructure(DataStructureRef ref) throws IOException {
        DataStructure result;

        try {
            result = delegate.getStructure(ref);
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while getting data structure from dataset '%s'", ref);
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null while getting data structure from dataset '%s'", ref);
        }

        return result;
    }

    @Override
    public DataCursor getData(DataflowRef flowRef, DataQuery query, DataStructure dsd) throws IOException {
        DataCursor result;

        try {
            result = delegate.getData(flowRef, query, dsd);
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while getting data from dataset '%s' with key '%s'", flowRef, query.getKey());
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null while getting data from dataset '%s' with key '%s'", flowRef, query.getKey());
        }

        return result;
    }

    @Override
    public boolean isSeriesKeysOnlySupported() throws IOException {
        try {
            return delegate.isSeriesKeysOnlySupported();
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while checking keys-only support");
        }
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef flowRef) throws IOException {
        try {
            return delegate.peekStructureRef(flowRef);
        } catch (RuntimeException ex) {
            throw unexpected(ex, "Unexpected exception while peeking struct ref for dataset '%s'", flowRef);
        }
    }

    private static IOException unexpected(RuntimeException ex, String format, Object... args) {
        log.log(Level.WARNING, String.format(format, args));
        return new UnexpectedIOException(ex);
    }

    private static IOException unexpectedNull(String format, Object... args) {
        String msg = String.format(format, args);
        log.log(Level.WARNING, msg);
        return new UnexpectedIOException(new NullPointerException(msg));
    }
}
