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
package be.nbb.sdmx.facade.repo;

import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.driver.SdmxDriver;
import be.nbb.sdmx.facade.driver.WsEntryPoint;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class SdmxRepositoryDriver implements SdmxDriver {

    @Nonnull
    public static SdmxRepositoryDriver of(@Nonnull SdmxRepository repo) {
        return new SdmxRepositoryDriver("sdmx:repo:", Collections.singletonList(repo));
    }

    @lombok.NonNull
    String prefix;

    @lombok.NonNull
    @lombok.Singular
    List<SdmxRepository> repositories;

    @Override
    public SdmxConnection connect(URI uri, Map<?, ?> info, LanguagePriorityList languages) throws IOException {
        return connect(getName(uri));
    }

    @Override
    public boolean acceptsURI(URI uri) throws IOException {
        return uri.toString().startsWith(prefix);
    }

    @Override
    public Collection<WsEntryPoint> getDefaultEntryPoints() {
        return repositories.stream()
                .map(o -> WsEntryPoint.builder().name(o.getName()).description("").uri(prefix + o.getName()).build())
                .collect(Collectors.toList());
    }

    private String getName(URI uri) throws IOException {
        String result = uri.toString();
        int index = result.indexOf(prefix);
        if (index == -1) {
            throw new IOException("Invalid URI '" + uri + "'");
        }
        return result.substring(prefix.length());
    }

    private SdmxConnection connect(String name) throws IOException {
        return repositories.stream()
                .filter(o -> o.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IOException("Cannot find '" + name + "'"))
                .asConnection();
    }
}
