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
package sdmxdl.file;

import nbbrd.design.NotThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.*;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@NotThreadSafe
public interface SdmxFileConnection extends SdmxConnection {

    @NonNull
    DataflowRef getDataflowRef() throws IOException;

    @NonNull
    default Dataflow getFlow() throws IOException {
        DataflowRef ref = getDataflowRef();
        return getFlows()
                .stream()
                .filter(o -> o.getRef().equals(ref))
                .findFirst()
                .orElseThrow(IOException::new);
    }

    @NonNull
    default DataStructure getStructure() throws IOException {
        return getStructure(getDataflowRef());
    }

    @NonNull
    default Collection<Series> getData(@NonNull Key key, @NonNull DataFilter filter) throws IOException {
        return getData(getDataflowRef(), key, filter);
    }

    @NonNull
    default Stream<Series> getDataStream(@NonNull Key key, @NonNull DataFilter filter) throws IOException {
        return getDataStream(getDataflowRef(), key, filter);
    }

    @NonNull
    default DataCursor getDataCursor(@NonNull Key key, @NonNull DataFilter filter) throws IOException {
        return getDataCursor(getDataflowRef(), key, filter);
    }
}
