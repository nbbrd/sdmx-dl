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
package be.nbb.sdmx.facade.web.spi;

import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.web.SdmxWebConnection;
import be.nbb.sdmx.facade.web.SdmxWebSource;
import java.io.IOException;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 *
 * @author Philippe Charles
 */
@ThreadSafe
public interface SdmxWebDriver {

    @Nonnull
    String getName();

    @Nonnull
    SdmxWebConnection connect(
            @Nonnull SdmxWebSource source,
            @Nonnull LanguagePriorityList languages,
            @Nonnull SdmxWebBridge bridge
    ) throws IOException, IllegalArgumentException;

    @Nonnull
    Collection<SdmxWebSource> getDefaultSources();

    @Nonnull
    Collection<String> getSupportedProperties();
}
