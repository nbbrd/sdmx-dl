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
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;

import java.io.IOException;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public final class SdmxExceptions {

    @NonNull
    public IOException connectionClosed(@NonNull String source) {
        return new IOException("Connection '" + source + "' already closed");
    }

    @NonNull
    public IOException missingFlow(@NonNull String source, @NonNull DataflowRef ref) {
        return new IOException("Missing dataflow '" + ref + "' in '" + source + "'");
    }

    @NonNull
    public IOException missingStructure(@NonNull String source, @NonNull DataStructureRef ref) {
        return new IOException("Missing datastructure '" + ref + "' in '" + source + "'");
    }

    @NonNull
    public IOException missingData(@NonNull String source, @NonNull DataflowRef ref) {
        return new IOException("Missing data '" + ref + "' in '" + source + "'");
    }
}
