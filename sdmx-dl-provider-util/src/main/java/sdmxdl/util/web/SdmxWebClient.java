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
package sdmxdl.util.web;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.*;
import sdmxdl.web.SdmxWebSource;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * @author Philippe Charles
 */
public interface SdmxWebClient {

    @NonNull
    String getName() throws IOException;

    @NonNull
    List<Dataflow> getFlows() throws IOException;

    @NonNull
    Dataflow getFlow(@NonNull DataflowRef ref) throws IOException;

    @NonNull
    DataStructure getStructure(@NonNull DataStructureRef ref) throws IOException;

    @NonNull
    DataCursor getData(@NonNull DataRequest request, @NonNull DataStructure dsd) throws IOException;

    @NonNull Codelist getCodelist(@NonNull CodelistRef ref) throws IOException;

    boolean isDetailSupported() throws IOException;

    @Nullable
    DataStructureRef peekStructureRef(@NonNull DataflowRef ref) throws IOException;

    @NonNull
    Duration ping() throws IOException;

    @NonNull
    static String getClientName(@NonNull SdmxWebSource source) {
        return source.getDriver() + ":" + source.getName();
    }
}
