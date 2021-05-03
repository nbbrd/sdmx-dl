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
import sdmxdl.web.SdmxWebConnection;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 *
 * @author Philippe Charles
 */
@lombok.extern.java.Log
@lombok.AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class FailsafeSdmxWebDriver implements SdmxWebDriver {

    public static SdmxWebDriver wrap(SdmxWebDriver obj) {
        return obj instanceof FailsafeSdmxWebDriver
                ? obj
                : new FailsafeSdmxWebDriver(obj,
                        FailsafeSdmxWebDriver::logUnexpectedError,
                        FailsafeSdmxWebDriver::logUnexpectedNull);
    }

    @lombok.NonNull
    private final SdmxWebDriver delegate;

    @lombok.NonNull
    private final BiConsumer<? super String, ? super RuntimeException> onUnexpectedError;

    @lombok.NonNull
    private final Consumer<? super String> onUnexpectedNull;

    @Override
    public String getName() {
        String result;

        try {
            result = delegate.getName();
        } catch (RuntimeException ex) {
            onUnexpectedError.accept("Unexpected exception while getting name", ex);
            return delegate.getClass().getName();
        }

        if (result == null) {
            onUnexpectedNull.accept("Unexpected null name");
            return delegate.getClass().getName();
        }

        return result;
    }

    @Override
    public int getRank() {
        try {
            return delegate.getRank();
        } catch (RuntimeException ex) {
            onUnexpectedError.accept("Unexpected exception while getting rank", ex);
            return UNKNOWN;
        }
    }

    @Override
    public SdmxWebConnection connect(SdmxWebSource source, SdmxWebContext context) throws IOException, IllegalArgumentException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(context);

        SdmxWebConnection result;

        try {
            result = delegate.connect(source, context);
        } catch (RuntimeException ex) {
            throw unexpectedError("Unexpected exception while connecting", ex);
        }

        if (result == null) {
            throw unexpectedNull("Unexpected null connection");
        }

        return FailsafeSdmxWebConnection.wrap(result);
    }

    @Override
    public Collection<SdmxWebSource> getDefaultSources() {
        Collection<SdmxWebSource> result;

        try {
            result = delegate.getDefaultSources();
        } catch (RuntimeException ex) {
            onUnexpectedError.accept("Unexpected exception while getting default entry points", ex);
            return Collections.emptyList();
        }

        if (result == null) {
            onUnexpectedNull.accept("Unexpected null list");
            return Collections.emptyList();
        }

        return result;
    }

    @Override
    public Collection<String> getSupportedProperties() {
        Collection<String> result;

        try {
            result = delegate.getSupportedProperties();
        } catch (RuntimeException ex) {
            onUnexpectedError.accept("Unexpected exception while getting supported properties", ex);
            return Collections.emptyList();
        }

        if (result == null) {
            onUnexpectedNull.accept("Unexpected null list");
            return Collections.emptyList();
        }

        return result;
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
