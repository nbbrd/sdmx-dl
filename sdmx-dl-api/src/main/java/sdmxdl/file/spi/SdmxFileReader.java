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
package sdmxdl.file.spi;

import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.file.SdmxFileConnection;
import sdmxdl.file.SdmxFileSource;

import java.io.IOException;

/**
 * @author Philippe Charles
 */
@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE
)
@ThreadSafe
public interface SdmxFileReader {

    @Nullable
    SdmxFileSource getSource(@NonNull String name);

    boolean canRead(@NonNull SdmxFileSource source);

    @NonNull
    SdmxFileConnection read(
            @NonNull SdmxFileSource source,
            @NonNull SdmxFileContext context
    ) throws IOException, IllegalArgumentException;
}
