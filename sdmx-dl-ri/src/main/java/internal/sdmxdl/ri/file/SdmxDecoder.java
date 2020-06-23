/*
 * Copyright 2015 National Bank of Belgium
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
package internal.sdmxdl.ri.file;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataStructure;
import sdmxdl.LanguagePriorityList;
import sdmxdl.file.SdmxFileSource;

import java.io.IOException;

/**
 * @author Philippe Charles
 */
public interface SdmxDecoder {

    @NonNull
    Info decode(@NonNull SdmxFileSource source, @NonNull LanguagePriorityList languages) throws IOException;

    @lombok.Value(staticConstructor = "of")
    class Info {

        String dataType;
        DataStructure structure;
    }
}
