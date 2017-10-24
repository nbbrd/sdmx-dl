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
package internal.connectors;

import be.nbb.sdmx.facade.LanguagePriorityList;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.client.RestSdmxClient;
import java.net.URI;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public interface GenericSDMXClientSupplier {

    @Nonnull
    GenericSDMXClient getClient(@Nonnull URI endpoint, @Nonnull Map<?, ?> info, @Nonnull LanguagePriorityList langs);

    @Nonnull
    static GenericSDMXClientSupplier ofType(@Nonnull Class<? extends RestSdmxClient> clazz) {
        return (URI endpoint, Map<?, ?> info, LanguagePriorityList langs) -> {
            try {
                RestSdmxClient result = clazz.newInstance();
                result.setEndpoint(endpoint);
                result.setLanguages(Util.fromLanguages(langs));
                return result;
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        };
    }
}
