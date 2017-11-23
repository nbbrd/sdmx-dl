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
package internal.web;

import be.nbb.sdmx.facade.DataCursor;
import be.nbb.sdmx.facade.DataQuery;
import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.DataStructureRef;
import be.nbb.sdmx.facade.Dataflow;
import be.nbb.sdmx.facade.DataflowRef;
import be.nbb.sdmx.facade.LanguagePriorityList;
import be.nbb.sdmx.facade.web.SdmxWebEntryPoint;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
public interface RestClient {

    @Nonnull
    List<Dataflow> getFlows() throws IOException;

    @Nonnull
    Dataflow getFlow(@Nonnull DataflowRef ref) throws IOException;

    @Nonnull
    DataStructure getStructure(@Nonnull DataStructureRef ref) throws IOException;

    @Nonnull
    DataCursor getData(@Nonnull DataflowRef flowRef, @Nonnull DataStructure dsd, @Nonnull DataQuery query) throws IOException;

    boolean isSeriesKeysOnlySupported();

    @Nullable
    DataStructureRef peekStructureRef(@Nonnull DataflowRef flowRef);

    interface Supplier {

        @Nonnull
        RestClient get(@Nonnull SdmxWebEntryPoint entryPoint, @Nonnull String prefix, @Nonnull LanguagePriorityList languages);
    }
}
