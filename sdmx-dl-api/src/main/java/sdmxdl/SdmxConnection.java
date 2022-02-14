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
package sdmxdl;

import nbbrd.design.NotThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@NotThreadSafe
public interface SdmxConnection extends Closeable {

    @NonNull
    Collection<Dataflow> getFlows() throws IOException;

    @NonNull
    Dataflow getFlow(@NonNull DataflowRef flowRef) throws IOException, IllegalArgumentException;

    @NonNull
    DataStructure getStructure(@NonNull DataflowRef flowRef) throws IOException, IllegalArgumentException;

    @NonNull
    Collection<Series> getData(@NonNull DataRef dataRef) throws IOException, IllegalArgumentException;

    @NonNull
    Stream<Series> getDataStream(@NonNull DataRef dataRef) throws IOException, IllegalArgumentException;

    boolean isDetailSupported() throws IOException;
}
