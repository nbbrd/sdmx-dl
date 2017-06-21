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
package be.nbb.sdmx.facade.file;

import be.nbb.sdmx.facade.DataStructure;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public interface SdmxDecoder {

    @Nonnull
    Info decode(@Nonnull SdmxFile file, @Nonnull List<Locale.LanguageRange> ranges) throws IOException;

    enum DataType {

        GENERIC20, GENERIC21, COMPACT20, COMPACT21, UNKNOWN
    }

    @lombok.Value(staticConstructor = "of")
    class Info {

        DataType dataType;
        DataStructure dataStructure;
    }
}
