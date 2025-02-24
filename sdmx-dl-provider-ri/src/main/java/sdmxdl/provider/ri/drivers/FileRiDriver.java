package sdmxdl.provider.ri.drivers;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.*;
import sdmxdl.ext.Cache;
import sdmxdl.ext.Persistence;
import sdmxdl.file.FileSource;
import sdmxdl.file.SdmxFileManager;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.provider.web.DriverSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Driver;
import sdmxdl.web.spi.WebCaching;
import sdmxdl.web.spi.WebContext;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.Collection;
import java.util.List;

@DirectImpl
@ServiceProvider
public final class FileRiDriver implements Driver {

    @PropertyDefinition
    public static final Property<URI> STRUCTURE_URI_PROPERTY
            = Property.of(DRIVER_PROPERTY_PREFIX + ".structureURI", null, Parser.onURI(), Formatter.onURI());

    private static final String RI_FILE = "RI_FILE";

    @PropertyDefinition
    private static final BooleanProperty ENABLE_PROPERTY =
            BooleanProperty.of("enableFileDriver", false);

    @lombok.experimental.Delegate
    private final DriverSupport support = DriverSupport
            .builder()
            .id(RI_FILE)
            .rank(NATIVE_DRIVER_RANK)
            .availability(ENABLE_PROPERTY::get)
            .connector(this::newConnection)
            .propertyOf(STRUCTURE_URI_PROPERTY)
            .build();

    private final SdmxFileManager fileManager = SdmxFileManager.ofServiceLoader();

    private @NonNull Connection newConnection(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) throws IOException, IllegalArgumentException {
        return fileManager
                .toBuilder()
                .onEvent(context.getOnEvent() != null ? (fileSource, marker, message) -> context.getOnEvent().accept(source, marker, message) : null)
                .caching(new FileCachingAdapter(context.getCaching(), source, context.getOnEvent(), context.getOnError()))
                .build()
                .getConnection(toFileSource(source), languages);
    }

    @lombok.AllArgsConstructor
    private static final class FileCachingAdapter implements FileCaching {

        private final @NonNull WebCaching delegate;

        private final @NonNull WebSource webSource;

        private final @Nullable EventListener<? super WebSource> onWebEvent;

        private final @Nullable ErrorListener<? super WebSource> onWebError;

        @Override
        public @NonNull String getFileCachingId() {
            return delegate.getWebCachingId();
        }

        @Override
        public int getFileCachingRank() {
            return delegate.getWebCachingRank();
        }

        @Override
        public @NonNull Cache<DataRepository> getReaderCache(
                @NonNull FileSource ignoreSource,
                @NonNull List<Persistence> persistences,
                @Nullable EventListener<? super FileSource> ignoreEvent,
                @Nullable ErrorListener<? super FileSource> ignoreError) {
            return new FileCacheAdapter(delegate.getDriverCache(webSource, persistences, onWebEvent, onWebError));
        }

        @Override
        public @NonNull Collection<String> getFileCachingProperties() {
            return delegate.getWebCachingProperties();
        }
    }

    @lombok.AllArgsConstructor
    private static final class FileCacheAdapter implements Cache<DataRepository> {

        private final @NonNull Cache<DataRepository> delegate;

        @Override
        public @NonNull Clock getClock() {
            return delegate.getClock();
        }

        @Override
        public @Nullable DataRepository get(@NonNull String key) {
            return delegate.get(key);
        }

        @Override
        public void put(@NonNull String key, @NonNull DataRepository value) {
            delegate.put(key, value);
        }
    }

    private static FileSource toFileSource(WebSource source) throws IOException {
        return FileSource
                .builder()
                .data(toFile(source.getEndpoint()))
                .structure(toFile(STRUCTURE_URI_PROPERTY.get(source.getProperties())))
                .build();
    }

    @VisibleForTesting
    static File toFile(URI endpoint) throws IOException {
        if (endpoint != null) {
            try {
                return Paths.get(endpoint).toFile();
            } catch (IllegalArgumentException ex) {
                throw new IOException("Invalid file name: '" + endpoint + "'", ex);
            }
        }
        return null;
    }
}
