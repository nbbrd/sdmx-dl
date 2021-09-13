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
import sdmxdl.*;
import sdmxdl.web.SdmxWebConnection;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
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
    public Duration ping() throws IOException {
        Duration result;

        try {
            result = delegate.ping();
        } catch (RuntimeException ex) {
            throw unexpectedError("Unexpected exception while getting ping", ex);
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null ping");
        }

        return result;
    }

    @Override
    public String getDriver() throws IOException {
        String result;

        try {
            result = delegate.getDriver();
        } catch (RuntimeException ex) {
            throw unexpectedError("Unexpected exception while getting driver", ex);
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null driver");
        }

        return result;
    }

    @Override
    public Collection<Dataflow> getFlows() throws IOException {
        Collection<Dataflow> result;

        try {
            result = delegate.getFlows();
        } catch (RuntimeException ex) {
            throw unexpectedError("Unexpected exception while getting flows", ex);
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null flows");
        }

        return result;
    }

    @Override
    public Dataflow getFlow(DataflowRef flowRef) throws IOException {
        Objects.requireNonNull(flowRef);

        Dataflow result;

        try {
            result = delegate.getFlow(flowRef);
        } catch (RuntimeException ex) {
            throw unexpectedError("Unexpected exception while getting flow", ex);
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null flow");
        }

        return result;
    }

    @Override
    public DataStructure getStructure(DataflowRef flowRef) throws IOException {
        Objects.requireNonNull(flowRef);

        DataStructure result;

        try {
            result = delegate.getStructure(flowRef);
        } catch (RuntimeException ex) {
            throw unexpectedError("Unexpected exception while getting structure", ex);
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null structure");
        }

        return result;
    }

    @Override
    public Collection<Series> getData(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
        Objects.requireNonNull(flowRef);
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);

        Collection<Series> result;

        try {
            result = delegate.getData(flowRef, key, filter);
        } catch (RuntimeException ex) {
            throw unexpectedError("Unexpected exception while getting data", ex);
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null data");
        }

        return result;
    }

    @Override
    public Stream<Series> getDataStream(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
        Objects.requireNonNull(flowRef);
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);

        Stream<Series> result;

        try {
            result = delegate.getDataStream(flowRef, key, filter);
        } catch (RuntimeException ex) {
            throw unexpectedError("Unexpected exception while getting data stream", ex);
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null data stream");
        }

        return result;
    }

    @Override
    public DataCursor getDataCursor(DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
        Objects.requireNonNull(flowRef);
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);

        DataCursor result;

        try {
            result = delegate.getDataCursor(flowRef, key, filter);
        } catch (RuntimeException ex) {
            throw unexpectedError("Unexpected exception while getting data cursor", ex);
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null data cursor");
        }

        return result;
    }

    @Override
    public boolean isDetailSupported() throws IOException {
        try {
            return delegate.isDetailSupported();
        } catch (RuntimeException ex) {
            throw unexpectedError("Unexpected exception while getting detail support", ex);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            delegate.close();
        } catch (RuntimeException ex) {
            throw unexpectedError("Unexpected exception while closing", ex);
        }
    }

    private IOException unexpectedError(String msg, RuntimeException ex) {
        onUnexpectedError.accept(msg, ex);
        return new IOException(msg, ex);
    }

    private IOException unexpectedNull(String msg) {
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
