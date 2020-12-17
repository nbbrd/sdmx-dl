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
package sdmxdl.repo;

import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxConnection;
import sdmxdl.SdmxManager;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class SdmxRepositoryManager implements SdmxManager {

    @lombok.NonNull
    @lombok.Singular
    List<SdmxRepository> repositories;

    @Override
    public SdmxConnection getConnection(String name) throws IOException {
        Objects.requireNonNull(name);

        return repositories.stream()
                .filter(o -> o.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IOException("Cannot find '" + name + "'"))
                .asConnection();
    }

    @Override
    public LanguagePriorityList getLanguages() {
        return LanguagePriorityList.ANY;
    }
}
