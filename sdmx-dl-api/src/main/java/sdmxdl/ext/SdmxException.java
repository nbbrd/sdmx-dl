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
package sdmxdl.ext;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Philippe Charles
 */
public final class SdmxException extends IOException {

    public static @NonNull SdmxException connectionClosed(@NonNull String source) {
        return new SdmxException(source, "Connection closed");
    }

    public static @NonNull SdmxException missingSource(@NonNull String source, @NonNull Class<?> type) {
        Objects.requireNonNull(source);
        return new SdmxException(source, "Missing " + type.getSimpleName() + " '" + source + "'");
    }

    public static @NonNull SdmxException missingFlow(@NonNull String source, @NonNull DataflowRef ref) {
        Objects.requireNonNull(ref);
        return new SdmxException(source, "Missing flow '" + ref + "'");
    }

    public static @NonNull SdmxException missingStructure(@NonNull String source, @NonNull DataStructureRef ref) {
        Objects.requireNonNull(ref);
        return new SdmxException(source, "Missing structure '" + ref + "'");
    }

    public static @NonNull SdmxException missingData(@NonNull String source, @NonNull DataRef dataRef) {
        Objects.requireNonNull(dataRef);
        return new SdmxException(source, "Missing data '" + dataRef.getFlowRef() + "'");
    }

    public static @NonNull SdmxException missingCodelist(@NonNull String source, @NonNull CodelistRef ref) {
        Objects.requireNonNull(ref);
        return new SdmxException(source, "Missing codelist '" + ref + "'");
    }

    public static @NonNull SdmxException invalidKey(@NonNull String source, @NonNull Key key, @NonNull String cause) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(cause);
        return new SdmxException(source, "Invalid key '" + key + "': " + cause);
    }

    @lombok.Getter
    @NonNull
    private final String source;

    private SdmxException(String source, String message) {
        super(message);
        this.source = Objects.requireNonNull(source);
    }
}
