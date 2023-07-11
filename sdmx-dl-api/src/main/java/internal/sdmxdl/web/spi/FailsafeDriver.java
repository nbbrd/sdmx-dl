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
import sdmxdl.Connection;
import sdmxdl.Languages;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class FailsafeDriver implements Driver {

    public static Driver wrap(Driver obj) {
        if (obj instanceof FailsafeDriver) return obj;
        FailsafeLogging logging = FailsafeLogging.of(FailsafeDriver.class);
        return new FailsafeDriver(obj, logging::logUnexpectedError, logging::logUnexpectedNull);
    }

    @lombok.NonNull
    private final Driver delegate;

    @lombok.NonNull
    private final BiConsumer<? super String, ? super RuntimeException> onUnexpectedError;

    @lombok.NonNull
    private final Consumer<? super String> onUnexpectedNull;

    @Override
    public @NonNull String getDriverId() {
        String result;

        try {
            result = delegate.getDriverId();
        } catch (RuntimeException ex) {
            unexpectedError("while getting id", ex);
            return delegate.getClass().getName();
        }

        if (result == null) {
            unexpectedNull("null id");
            return delegate.getClass().getName();
        }

        return result;
    }

    @Override
    public int getDriverRank() {
        try {
            return delegate.getDriverRank();
        } catch (RuntimeException ex) {
            unexpectedError("while getting rank", ex);
            return UNKNOWN_DRIVER_RANK;
        }
    }

    @Override
    public boolean isDriverAvailable() {
        try {
            return delegate.isDriverAvailable();
        } catch (RuntimeException ex) {
            unexpectedError("while getting availability", ex);
            return false;
        }
    }

    @Override
    public @NonNull Connection connect(@NonNull SdmxWebSource source, @NonNull Languages languages, @NonNull WebContext context) throws IOException, IllegalArgumentException {
        Connection result;

        try {
            result = delegate.connect(source, languages, context);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw newUnexpectedError("while connecting", ex);
        }

        if (result == null) {
            throw newUnexpectedNull("null connection");
        }

        return FailsafeConnection.wrap(result);
    }

    @Override
    public @NonNull Collection<SdmxWebSource> getDefaultSources() {
        Collection<SdmxWebSource> result;

        try {
            result = delegate.getDefaultSources();
        } catch (RuntimeException ex) {
            unexpectedError("while getting default entry points", ex);
            return Collections.emptyList();
        }

        if (result == null) {
            unexpectedNull("null list");
            return Collections.emptyList();
        }

        return result;
    }

    @Override
    public @NonNull Collection<String> getDriverProperties() {
        Collection<String> result;

        try {
            result = delegate.getDriverProperties();
        } catch (RuntimeException ex) {
            unexpectedError("while getting supported properties", ex);
            return Collections.emptyList();
        }

        if (result == null) {
            unexpectedNull("null list");
            return Collections.emptyList();
        }

        return result;
    }

    private IOException newUnexpectedError(String context, RuntimeException ex) {
        String msg = "Unexpected " + ex.getClass().getSimpleName() + " " + context;
        onUnexpectedError.accept(msg, ex);
        return new IOException(msg, ex);
    }

    private IOException newUnexpectedNull(String context) {
        String msg = "Unexpected null " + context;
        onUnexpectedNull.accept(msg);
        return new IOException(msg);
    }

    private void unexpectedError(String context, RuntimeException ex) {
        String msg = "Unexpected " + ex.getClass().getSimpleName() + " " + context;
        onUnexpectedError.accept(msg, ex);
    }

    private void unexpectedNull(String context) {
        String msg = "Unexpected null " + context;
        onUnexpectedNull.accept(msg);
    }
}
