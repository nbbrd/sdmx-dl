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

import nbbrd.design.SealedType;
import nbbrd.design.ThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.ext.SdmxCache;
import sdmxdl.ext.spi.SdmxDialect;
import sdmxdl.file.SdmxFileManager;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.util.List;

/**
 * @author Philippe Charles
 */
@SealedType({
        SdmxFileManager.class,
        SdmxWebManager.class
})
@ThreadSafe
public abstract class SdmxManager<SOURCE> {

    public abstract @NonNull SdmxConnection getConnection(@NonNull SOURCE source) throws IOException;

    public abstract @NonNull LanguagePriorityList getLanguages();

    public abstract @NonNull SdmxCache getCache();

    public abstract @NonNull List<SdmxDialect> getDialects();
}
