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

import lombok.NonNull;
import nbbrd.design.NonNegative;
import nbbrd.design.NotThreadSafe;
import org.jspecify.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@NotThreadSafe
public interface Connection extends Closeable {

    @NonNull
    Optional<URI> testConnection() throws IOException;

    @NonNull
    Collection<Database> getDatabases() throws IOException;

    @NonNull
    Collection<Flow> getFlows(@NonNull DatabaseRef database) throws IOException, IllegalArgumentException;

    @NonNull
    MetaSet getMeta(@NonNull DatabaseRef database, @NonNull FlowRef flowRef) throws IOException, IllegalArgumentException;

    @NonNull
    DataSet getData(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException, IllegalArgumentException;

    @NonNull
    Stream<Series> getDataStream(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Query query) throws IOException, IllegalArgumentException;

    @NonNull
    Collection<String> getAvailableDimensionCodes(@NonNull DatabaseRef database, @NonNull FlowRef flowRef, @NonNull Key constraints, @NonNegative int dimensionIndex) throws IOException, IllegalArgumentException;

    @NonNull
    Set<Feature> getSupportedFeatures() throws IOException;
}
