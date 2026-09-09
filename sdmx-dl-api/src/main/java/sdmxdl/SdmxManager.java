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
package sdmxdl;

import java.io.IOException;
import java.util.function.Function;
import lombok.NonNull;
import nbbrd.design.SealedType;
import nbbrd.design.ThreadSafe;
import org.jspecify.annotations.Nullable;
import sdmxdl.file.SdmxFileManager;
import sdmxdl.web.SdmxWebManager;

/**
 * @author Philippe Charles
 */
@SealedType({SdmxFileManager.class, SdmxWebManager.class})
@ThreadSafe
public abstract class SdmxManager<SOURCE extends Source> {

    public final @NonNull Provider<SOURCE> using(@NonNull SOURCE source) {
        return new Provider<>(this, source);
    }

    public abstract @NonNull Connection getConnection(@NonNull SOURCE source, @NonNull Languages languages)
            throws IOException;

    public abstract @Nullable Function<? super SOURCE, EventListener> getOnEvent();

    public abstract @Nullable Function<? super SOURCE, ErrorListener> getOnError();
}
