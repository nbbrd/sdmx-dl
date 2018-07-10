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
package be.nbb.sdmx.facade;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

/**
 *
 * @author Philippe Charles
 */
@NotThreadSafe
public interface SdmxConnection extends Closeable {

    @Nonnull
    Set<Dataflow> getFlows() throws IOException;

    @Nonnull
    Dataflow getFlow(@Nonnull DataflowRef flowRef) throws IOException;

    @Nonnull
    DataStructure getStructure(@Nonnull DataflowRef flowRef) throws IOException;

    @Nonnull
    DataCursor getCursor(@Nonnull DataflowRef flowRef, @Nonnull Key key, @Nonnull DataFilter filter) throws IOException;

    @Nonnull
    Stream<Series> getStream(@Nonnull DataflowRef flowRef, @Nonnull Key key, @Nonnull DataFilter filter) throws IOException;

    boolean isSeriesKeysOnlySupported() throws IOException;
}
