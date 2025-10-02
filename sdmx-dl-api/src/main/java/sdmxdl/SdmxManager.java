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
import nbbrd.design.SealedType;
import nbbrd.design.ThreadSafe;
import org.jspecify.annotations.Nullable;
import sdmxdl.file.SdmxFileManager;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Philippe Charles
 */
@SealedType({
        SdmxFileManager.class,
        SdmxWebManager.class
})
@ThreadSafe
public abstract class SdmxManager<SOURCE extends Source> {

    public final @NonNull Provider using(@NonNull SOURCE source) {
        return new DefaultProvider<>(this, source);
    }

    public abstract @NonNull Connection getConnection(@NonNull SOURCE source, @NonNull Languages languages) throws IOException;

    public abstract @Nullable Function<? super SOURCE, EventListener> getOnEvent();

    public abstract @Nullable Function<? super SOURCE, ErrorListener> getOnError();

    @lombok.AllArgsConstructor
    private static final class DefaultProvider<SOURCE extends Source> implements Provider {

        @NonNull
        private final SdmxManager<SOURCE> manager;

        @NonNull
        private final SOURCE source;

        @Override
        public @NonNull Set<Feature> getSupportedFeatures(sdmxdl.@NonNull SourceRequest request) throws IOException {
            try (Connection connection = manager.getConnection(source, request.getLanguages())) {
                return connection.getSupportedFeatures();
            }
        }

        @Override
        public @NonNull Collection<Database> getDatabases(sdmxdl.@NonNull SourceRequest request) throws IOException {
            try (Connection connection = manager.getConnection(source, request.getLanguages())) {
                return connection.getDatabases();
            }
        }

        @Override
        public @NonNull Collection<Flow> getFlows(sdmxdl.@NonNull DatabaseRequest request) throws IOException {
            try (Connection connection = manager.getConnection(source, request.getLanguages())) {
                return connection.getFlows(request.getDatabase());
            }
        }

        @Override
        public @NonNull MetaSet getMeta(@NonNull FlowRequest request) throws IOException {
            try (Connection connection = manager.getConnection(source, request.getLanguages())) {
                return connection.getMeta(request.getDatabase(), request.getFlow());
            }
        }

        @Override
        public @NonNull DataSet getData(sdmxdl.@NonNull KeyRequest request) throws IOException {
            try (Connection connection = manager.getConnection(source, request.getLanguages())) {
                return connection.getData(request.getDatabase(), request.getFlow(), request.toQuery());
            }
        }
    }
}
