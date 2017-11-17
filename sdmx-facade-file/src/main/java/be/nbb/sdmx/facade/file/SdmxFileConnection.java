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
package be.nbb.sdmx.facade.file;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.SdmxConnection;
import be.nbb.sdmx.facade.Series;
import java.io.IOException;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public interface SdmxFileConnection extends SdmxConnection {

    @Nonnull
    DataflowRef getDataflowRef() throws IOException;

    @Nonnull
    default Dataflow getFlow() throws IOException {
        DataflowRef ref = getDataflowRef();
        return getFlows()
                .stream()
                .filter(o -> o.getRef().equals(ref))
                .findFirst()
                .orElseThrow(IOException::new);
    }

    @Nonnull
    default DataStructure getStructure() throws IOException {
        return getStructure(getDataflowRef());
    }

    @Nonnull
    default DataCursor getCursor(@Nonnull DataQuery query) throws IOException {
        return getCursor(getDataflowRef(), query);
    }

    @Nonnull
    default Stream<Series> getStream(@Nonnull DataQuery query) throws IOException {
        return getStream(getDataflowRef(), query);
    }
}
