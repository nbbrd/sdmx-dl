/*
 * Copyright 2020 National Bank of Belgium
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
package internal.sdmxdl.cli;

import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.web.SdmxWebConnection;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.time.Duration;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.Value
public class WebStatus {

    public static @NonNull WebStatus of(@NonNull SdmxWebManager manager, @NonNull String source) {
        try (final SdmxWebConnection conn = manager.getConnection(source)) {
            return success(source, conn.ping());
        } catch (IOException ex) {
            return failure(source, ex);
        }
    }

    public static @NonNull WebStatus success(@NonNull String source, @NonNull Duration duration) {
        return new WebStatus(source, duration, null);
    }

    public static @NonNull WebStatus failure(@NonNull String source, @NonNull IOException cause) {
        return new WebStatus(source, null, cause.getMessage());
    }

    @lombok.NonNull
    String source;

    @Nullable
    Duration duration;

    @Nullable
    String cause;

    public boolean isSuccess() {
        return cause == null;
    }
}
