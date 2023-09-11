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
import nbbrd.design.NotThreadSafe;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@NotThreadSafe
public interface Connection extends Closeable {

    void testConnection() throws IOException;

    @NonNull Collection<Flow> getFlows() throws IOException;

    @NonNull Flow getFlow(@NonNull FlowRef flowRef) throws IOException, IllegalArgumentException;

    @NonNull Structure getStructure(@NonNull FlowRef flowRef) throws IOException, IllegalArgumentException;

    @NonNull DataSet getData(@NonNull FlowRef flowRef, @NonNull Query query) throws IOException, IllegalArgumentException;

    @NonNull Stream<Series> getDataStream(@NonNull FlowRef flowRef, @NonNull Query query) throws IOException, IllegalArgumentException;

    @NonNull Set<Feature> getSupportedFeatures() throws IOException;
}
