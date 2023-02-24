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
package sdmxdl.provider;

import lombok.NonNull;
import sdmxdl.CodelistRef;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;

import java.io.IOException;

/**
 * @author Philippe Charles
 */
public final class CommonSdmxExceptions {

    public static @NonNull IOException connectionClosed(@NonNull HasMarker source) {
        return new IOException(String.format("Connection closed from '%s'", source.getMarker()));
    }

    public static @NonNull IOException missingFlow(@NonNull HasMarker source, @NonNull DataflowRef ref) {
        return new IOException(String.format("Missing flow '%s' from '%s'", ref, source.getMarker()));
    }

    public static @NonNull IOException missingStructure(@NonNull HasMarker source, @NonNull DataStructureRef ref) {
        return new IOException(String.format("Missing structure '%s' from '%s'", ref, source.getMarker()));
    }

    public static @NonNull IOException missingCodelist(@NonNull HasMarker source, @NonNull CodelistRef ref) {
        return new IOException(String.format("Missing codelist '%s' from '%s'", ref, source.getMarker()));
    }
}
