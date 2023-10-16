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

import lombok.NonNull;
import nbbrd.design.ThreadSafe;
import nbbrd.service.*;
import sdmxdl.Connection;
import sdmxdl.Languages;
import sdmxdl.file.FileSource;

import java.io.IOException;

/**
 * @author Philippe Charles
 */
@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        loaderName = "internal.util.ReaderLoader"
)
@ThreadSafe
public interface Reader {

    @ServiceId
    @NonNull String getReaderId();

    @ServiceSorter(reverse = true)
    int getReaderRank();

    @ServiceFilter
    boolean isReaderAvailable();

    boolean canRead(@NonNull FileSource source);

    @NonNull Connection read(
            @NonNull FileSource source,
            @NonNull Languages languages,
            @NonNull FileContext context
    ) throws IOException, IllegalArgumentException;

    int UNKNOWN_READER_RANK = -1;
}
