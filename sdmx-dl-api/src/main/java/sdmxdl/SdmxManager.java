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

import lombok.NonNull;
import nbbrd.design.SealedType;
import nbbrd.design.ThreadSafe;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.ext.spi.CacheProvider;
import sdmxdl.file.SdmxFileManager;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.util.Optional;

/**
 * @author Philippe Charles
 */
@SealedType({
        SdmxFileManager.class,
        SdmxWebManager.class
})
@ThreadSafe
public abstract class SdmxManager<SOURCE extends SdmxSource> {

    public abstract @NonNull Connection getConnection(@NonNull SOURCE source) throws IOException;

    public abstract @NonNull LanguagePriorityList getLanguages();

    public abstract @NonNull CacheProvider getCacheProvider();

    public abstract @NonNull SdmxSourceConsumer<? super SOURCE, ? super String> getEventListener();

    public abstract @NonNull Optional<String> getDialect(@NonNull SOURCE source);

    public static final SdmxSourceConsumer<SdmxSource, String> NO_OP_EVENT_LISTENER = (source, t) -> {
    };
}
