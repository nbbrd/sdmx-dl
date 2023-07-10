package internal.sdmxdl.provider.ri.web.drivers;

import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.Connection;
import sdmxdl.DataRepository;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.web.spi.WebCaching;
import sdmxdl.file.FileCache;
import sdmxdl.file.SdmxFileManager;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.provider.web.WebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.WebCache;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.util.Collection;

@ServiceProvider
public final class FileDriver implements WebDriver {

    private static final String RI_FILE = "ri:file";

    private static final BooleanProperty ENABLE =
            BooleanProperty.of("enableFileDriver", false);

    @lombok.experimental.Delegate
    private final WebDriverSupport support = WebDriverSupport
            .builder()
            .id(RI_FILE)
            .rank(NATIVE_RANK)
            .availability(ENABLE::get)
            .connector(this::newConnection)
            .supportedPropertyOf(STRUCTURE_PROPERTY)
            .build();

    private final SdmxFileManager fileManager = SdmxFileManager.ofServiceLoader();

    private @NonNull Connection newConnection(@NonNull SdmxWebSource source, @NonNull WebContext context) throws IOException, IllegalArgumentException {
        return fileManager
                .toBuilder()
                .languages(context.getLanguages())
                .eventListener((fileSource, message) -> context.getEventListener().accept(source, message))
                .caching(new FileCachingAdapter(source, context.getCaching(), context.getEventListener()))
                .build()
                .getConnection(toFileSource(source));
    }

    @lombok.AllArgsConstructor
    private static final class FileCachingAdapter implements FileCaching {

        private final @NonNull SdmxWebSource webSource;

        private final @NonNull WebCaching delegate;

        private final @NonNull SdmxSourceConsumer<? super SdmxWebSource, ? super String> webListener;

        @Override
        public @NonNull String getFileCachingId() {
            return delegate.getWebCachingId();
        }

        @Override
        public int getFileCachingRank() {
            return delegate.getWebCachingRank();
        }

        @Override
        public @NonNull FileCache getFileCache(@NonNull SdmxFileSource ignoreSource, @NonNull SdmxSourceConsumer<? super SdmxFileSource, ? super String> ignoreEvent) {
            return new FileCacheAdapter(delegate.getWebCache(webSource, webListener));
        }

        @Override
        public @NonNull Collection<String> getFileCachingProperties() {
            return delegate.getWebCachingProperties();
        }
    }

    @lombok.AllArgsConstructor
    private static final class FileCacheAdapter implements FileCache {

        private final @NonNull WebCache delegate;

        @Override
        public @NonNull Clock getFileClock() {
            return delegate.getWebClock();
        }

        @Override
        public @Nullable DataRepository getFileRepository(@NonNull String key) {
            return delegate.getWebRepository(key);
        }

        @Override
        public void putFileRepository(@NonNull String key, @NonNull DataRepository value) {
            delegate.putWebRepository(key, value);
        }
    }

    private static SdmxFileSource toFileSource(SdmxWebSource source) throws IOException {
        return SdmxFileSource
                .builder()
                .data(toFile(source.getEndpoint()))
                .structure(toFile(STRUCTURE_PROPERTY.get(source.getProperties())))
                .build();
    }

    @VisibleForTesting
    static File toFile(URI endpoint) throws IOException {
        if (endpoint != null) {
            try {
                return new File(endpoint);
            } catch (IllegalArgumentException ex) {
                throw new IOException("Invalid file name: '" + endpoint + "'", ex);
            }
        }
        return null;
    }

    private static final Property<URI> STRUCTURE_PROPERTY =
            Property.of("structureURL", null, Parser.onURI(), Formatter.onURI());
}
