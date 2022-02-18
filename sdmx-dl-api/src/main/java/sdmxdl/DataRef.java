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
package sdmxdl;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class DataRef {

    public static @NonNull DataRef of(@NonNull DataflowRef flowRef, @NonNull Key key, @NonNull DataFilter filter) {
        Objects.requireNonNull(flowRef);
        Objects.requireNonNull(key);
        Objects.requireNonNull(filter);
        return new DataRef(flowRef, key, filter);
    }

    public static @NonNull DataRef of(@NonNull DataflowRef flowRef) {
        return of(flowRef, Key.ALL, DataFilter.FULL);
    }

    @lombok.NonNull
    DataflowRef flowRef;

    @lombok.NonNull
    Key key;

    @lombok.NonNull
    DataFilter filter;
}
