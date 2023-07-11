package internal.sdmxdl.provider.ri.web.drivers;

import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.*;
import sdmxdl.file.FileCache;
import sdmxdl.file.SdmxFileManager;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.provider.web.WebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.WebCache;
import sdmxdl.web.spi.WebCaching;
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

    private @NonNull Connection newConnection(@NonNull SdmxWebSource source, @NonNull LanguagePriorityList languages, @NonNull WebContext context) throws IOException, IllegalArgumentException {
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

        private final @NonNull SdmxWebSource webSource;

        private final @Nullable EventListener<? super SdmxWebSource> onWebEvent;

        private final @Nullable ErrorListener<? super SdmxWebSource> onWebError;

        @Override
        public @NonNull String getFileCachingId() {
            return delegate.getWebCachingId();
        }

        @Override
        public int getFileCachingRank() {
            return delegate.getWebCachingRank();
        }

        @Override
        public @NonNull FileCache getFileCache(@NonNull SdmxFileSource ignoreSource, @Nullable EventListener<? super SdmxFileSource> ignoreEvent, @Nullable ErrorListener<? super SdmxFileSource> ignoreError) {
            return new FileCacheAdapter(delegate.getWebCache(webSource, onWebEvent, onWebError));
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
