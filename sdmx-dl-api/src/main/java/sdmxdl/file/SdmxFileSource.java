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
package sdmxdl.file;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataflowRef;
import sdmxdl.SdmxSource;

import java.io.File;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.EqualsAndHashCode(callSuper = false)
public class SdmxFileSource extends SdmxSource {

    @lombok.NonNull
    File data;

    @Nullable
    File structure;

    @NonNull
    public DataflowRef asDataflowRef() {
        return DataflowRef.parse("data" + (structure != null && !structure.toString().isEmpty() ? "&struct" : ""));
    }

    @NonNull
    public static String asFlowLabel(@NonNull SdmxFileSource source) {
        return source.getData().getName().replace(".xml", "");
    }
}
