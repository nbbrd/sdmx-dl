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
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;
import sdmxdl.web.SdmxWebConnection;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("ConstantConditions")
@lombok.extern.java.Log
@lombok.AllArgsConstructor(access = AccessLevel.PACKAGE)
final class FailsafeSdmxWebConnection implements SdmxWebConnection {

    static SdmxWebConnection wrap(SdmxWebConnection obj) {
        return obj instanceof FailsafeSdmxWebConnection
                ? obj
                : new FailsafeSdmxWebConnection(obj,
                FailsafeSdmxWebConnection::logUnexpectedError,
                FailsafeSdmxWebConnection::logUnexpectedNull
        );
    }

    static SdmxWebConnection unwrap(SdmxWebConnection obj) {
        return obj instanceof FailsafeSdmxWebConnection
                ? ((FailsafeSdmxWebConnection) obj).delegate
                : obj;
    }

    @lombok.NonNull
    private final SdmxWebConnection delegate;

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
    public String getDriver() throws IOException {
        String result;

        try {
            result = delegate.getDriver();
        } catch (RuntimeException ex) {
            throw unexpectedError(ex, "while getting driver");
        }

        if (result == null) {
            throw unexpectedNull("driver");
        }

        return result;
    }

    @Override
    public Collection<Dataflow> getFlows() throws IOException {
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
    public Dataflow getFlow(DataflowRef flowRef) throws IOException {
        Objects.requireNonNull(flowRef);

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
    public DataStructure getStructure(DataflowRef flowRef) throws IOException {
        Objects.requireNonNull(flowRef);

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
    public DataSet getData(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
        Objects.requireNonNull(flowRef);
        Objects.requireNonNull(query);

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
    public Stream<Series> getDataStream(@NonNull DataflowRef flowRef, @NonNull DataQuery query) throws IOException {
        Objects.requireNonNull(flowRef);
        Objects.requireNonNull(query);

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
    public Set<Feature> getSupportedFeatures() throws IOException {
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

    private static void logUnexpectedError(String msg, RuntimeException ex) {
        if (log.isLoggable(Level.WARNING)) {
            log.log(Level.WARNING, msg, ex);
        }
    }

    private static void logUnexpectedNull(String msg) {
        if (log.isLoggable(Level.WARNING)) {
            log.log(Level.WARNING, msg);
        }
    }
}
