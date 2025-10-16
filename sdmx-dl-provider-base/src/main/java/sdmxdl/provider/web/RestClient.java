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
package sdmxdl.provider.web;

import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.HasMarker;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
public interface RestClient extends HasMarker {

    @NonNull
    List<Flow> getFlows() throws IOException;

    @NonNull
    Flow getFlow(@NonNull FlowRef ref) throws IOException;

    @NonNull
    Structure getStructure(@NonNull StructureRef ref) throws IOException;

    @NonNull
    Stream<Series> getData(@NonNull DataRef ref, @NonNull Structure dsd) throws IOException;

    @NonNull
    Codelist getCodelist(@NonNull CodelistRef ref) throws IOException;

    @NonNull
    Set<Feature> getSupportedFeatures() throws IOException;

    @NonNull
    Optional<URI> testClient() throws IOException;
}
