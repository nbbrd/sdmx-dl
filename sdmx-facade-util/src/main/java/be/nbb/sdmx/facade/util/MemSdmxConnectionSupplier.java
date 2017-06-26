/*
 * Copyright 2016 National Bank of Belgium
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
package be.nbb.sdmx.facade.util;

import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.SdmxConnectionSupplier;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class MemSdmxConnectionSupplier implements SdmxConnectionSupplier {

    @lombok.NonNull
    @lombok.Singular
    List<MemSdmxRepository> repositories;

    @Override
    public SdmxConnection getConnection(String name, LanguagePriorityList languages) throws IOException {
        return repositories.stream()
                .filter(o -> o.getName().equals(name))
                .map(MemSdmxRepository::asConnection)
                .findFirst()
                .orElseThrow(() -> new IOException(name));
    }
}
