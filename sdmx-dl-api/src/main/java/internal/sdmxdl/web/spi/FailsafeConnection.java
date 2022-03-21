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
package internal.sdmxdl.web.spi;

import lombok.AccessLevel;
import lombok.NonNull;
import sdmxdl.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("ConstantConditions")
@lombok.AllArgsConstructor(access = AccessLevel.PACKAGE)
final class FailsafeConnection implements Connection {

    static Connection wrap(Connection obj) {
        if (obj instanceof FailsafeConnection) return obj;
        FailsafeLogging logging = FailsafeLogging.of(FailsafeDriver.class);
        return new FailsafeConnection(obj, logging::logUnexpectedError, logging::logUnexpectedNull);
    }

    @lombok.NonNull
    private final Connection delegate;

    @lombok.NonNull
    private final BiConsumer<? super String, ? super RuntimeException> onUnexpectedError;

    @lombok.NonNull
    private final Consumer<? super String> onUnexpectedNull;

    @Override
    public void testConnection() throws IOException {
        try {
            delegate.testConnection();
        } catch (RuntimeException ex) {
            throw unexpectedError(ex, "while testing connection");
        }
    }

    @Override
    public @NonNull Collection<Dataflow> getFlows() throws IOException {
        Collection<Dataflow> result;

        try {
            result = delegate.getFlows();
        } catch (RuntimeException ex) {
            throw unexpectedError(ex, "while getting flows");
        }

        if (result == null) {
            throw unexpectedNull("flows");
        }

        return result;
    }

    @Override
    public @NonNull Dataflow getFlow(@NonNull DataflowRef flowRef) throws IOException {
        Dataflow result;

        try {
            result = delegate.getFlow(flowRef);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw unexpectedError(ex, "while getting flow");
        }

        if (result == null) {
            throw unexpectedNull("flow");
        }

        return result;
    }

    @Override
    public @NonNull DataStructure getStructure(@NonNull DataflowRef flowRef) throws IOException {
        DataStructure result;

        try {
            result = delegate.getStructure(flowRef);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw unexpectedError(ex, "while getting structure");
        }

        if (result == null) {
            throw unexpectedNull("structure");
        }

        return result;
    }

    @Override
    public @NonNull DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
        DataSet result;

        try {
            result = delegate.getData(flowRef, query);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw unexpectedError(ex, "while getting data");
        }

        if (result == null) {
            throw unexpectedNull("data");
        }

        return result;
    }

    @Override
    public @NonNull Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
        Stream<Series> result;

        try {
            result = delegate.getDataStream(flowRef, query);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw unexpectedError(ex, "while getting data stream");
        }

        if (result == null) {
            throw unexpectedNull("data stream");
        }

        return result;
    }

    @Override
    public @NonNull Set<Feature> getSupportedFeatures() throws IOException {
        Set<Feature> result;

        try {
            result = delegate.getSupportedFeatures();
        } catch (RuntimeException ex) {
            throw unexpectedError(ex, "while getting supported features");
        }

        if (result == null) {
            throw unexpectedNull("supported features");
        }

        return result;
    }

    @Override
    public void close() throws IOException {
        try {
            delegate.close();
        } catch (RuntimeException ex) {
            throw unexpectedError(ex, "while closing");
        }
    }

    private IOException unexpectedError(RuntimeException ex, String context) {
        String msg = "Unexpected " + ex.getClass().getSimpleName() + " " + context;
        onUnexpectedError.accept(msg, ex);
        return new IOException(msg, ex);
    }

    private IOException unexpectedNull(String context) {
        String msg = "Unexpected null " + context;
        onUnexpectedNull.accept(msg);
        return new IOException(msg);
    }
}
