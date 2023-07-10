package internal.sdmxdl.provider.ri.web.drivers;

import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import nbbrd.service.ServiceProvider;
import sdmxdl.Connection;
import sdmxdl.ext.Cache;
import sdmxdl.ext.SdmxSourceConsumer;
import sdmxdl.ext.spi.Caching;
import sdmxdl.file.SdmxFileManager;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.provider.web.WebDriverSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;
import sdmxdl.web.spi.WebDriver;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;

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
                .caching(new FileToWebCacheFactory(source, context.getCaching(), context.getEventListener()))
                .build()
                .getConnection(toFileSource(source));
    }

    @lombok.AllArgsConstructor
    private static final class FileToWebCacheFactory implements Caching {

        private final @NonNull SdmxWebSource webSource;

        private final @NonNull Caching webCacheFactory;

        private final @NonNull SdmxSourceConsumer<? super SdmxWebSource, ? super String> eventListener;

        private Cache getCache() {
            return webCacheFactory.getWebCache(webSource, eventListener);
        }

        @Override
        public @NonNull String getCachingId() {
            return webCacheFactory.getCachingId();
        }

        @Override
        public int getCachingRank() {
            return webCacheFactory.getCachingRank();
        }

        @Override
        public @NonNull Cache getFileCache(@NonNull SdmxFileSource ignoreSource, @NonNull SdmxSourceConsumer<? super SdmxFileSource, ? super String> ignoreEvent) {
            return getCache();
        }

        @Override
        public @NonNull Cache getWebCache(@NonNull SdmxWebSource ignoreSource, @NonNull SdmxSourceConsumer<? super SdmxWebSource, ? super String> ignoreEvent) {
            return getCache();
        }

        @Override
        public @NonNull Collection<String> getFileCachingProperties() {
            return Collections.emptyList();
        }

        @Override
        public @NonNull Collection<String> getWebCachingProperties() {
            return webCacheFactory.getWebCachingProperties();
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
