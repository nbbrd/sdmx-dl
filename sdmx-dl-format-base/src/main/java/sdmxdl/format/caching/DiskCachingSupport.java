package sdmxdl.format.caching;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.jspecify.annotations.Nullable;
import sdmxdl.*;
import sdmxdl.ext.Cache;
import sdmxdl.ext.FileFormat;
import sdmxdl.file.FileSource;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.format.PropertiesSupport;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.format.design.ServiceSupport;
import sdmxdl.web.Credentials;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebCaching;

import java.io.File;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Collection;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static sdmxdl.format.FileFormatSupport.*;

@ServiceSupport(FileCaching.class)
@ServiceSupport(WebCaching.class)
@lombok.Builder(toBuilder = true)
public final class DiskCachingSupport implements FileCaching, WebCaching {

    // Set cache folder
    @PropertyDefinition
    public static final Property<File> CACHE_FOLDER_PROPERTY
            = Property.of("sdmxdl.caching.cacheFolder", null, Parser.onFile(), Formatter.onFile());

    // Disable cache compression
    @PropertyDefinition
    public static final BooleanProperty NO_COMPRESSION_PROPERTY
            = BooleanProperty.of("sdmxdl.caching.noCompression", false);

    @lombok.Getter(AccessLevel.PRIVATE)
    private final @NonNull String id;

    @lombok.Builder.Default
    private final int rank = UNKNOWN_WEB_CACHING_RANK;

    @lombok.Builder.Default
    private final @NonNull Clock clock = Clock.systemDefaultZone();

    @lombok.Builder.Default
    private final @NonNull FileFormat<DataRepository> repositoryFormat = FileFormat.noOp();

    @lombok.Builder.Default
    private final @NonNull FileFormat<MonitorReports> monitorsFormat = FileFormat.noOp();

    @Override
    public @NonNull String getFileCachingId() {
        return id;
    }

    @Override
    public @NonNull String getWebCachingId() {
        return id;
    }

    @Override
    public int getFileCachingRank() {
        return rank;
    }

    @Override
    public int getWebCachingRank() {
        return rank;
    }

    @Override
    public @NonNull Cache<DataRepository> getReaderCache(
            @NonNull FileSource source,
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError) {
        Function<? super String, ? extends CharSequence> properties = PropertiesSupport.asFunction(source);
        Path root = getCacheFolder(properties);

        logConfig(onEvent, repositoryFormat, root);
        return decorateCache(DiskCache
                .<DataRepository>builder()
                .id(id)
                .root(root)
                .format(decorateFormat(repositoryFormat, isNoCompression(properties)))
                .namePrefix("R")
                .clock(clock)
                .onEvent(onEvent)
                .onError(onError)
                .build());
    }

    @Override
    public @NonNull Cache<DataRepository> getDriverCache(
            @NonNull WebSource source,
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError) {
        Function<? super String, ? extends CharSequence> properties = PropertiesSupport.asFunction(source);
        Path root = getCacheFolder(properties);

        logConfig(onEvent, repositoryFormat, root);
        return decorateCache(DiskCache
                .<DataRepository>builder()
                .id(id)
                .root(root)
                .format(decorateFormat(repositoryFormat, isNoCompression(properties)))
                .namePrefix("D")
                .clock(clock)
                .onEvent(onEvent)
                .onError(onError)
                .build());
    }

    @Override
    public @NonNull Cache<MonitorReports> getMonitorCache(
            @NonNull WebSource source,
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError) {
        Function<? super String, ? extends CharSequence> properties = PropertiesSupport.asFunction(source);
        Path root = getCacheFolder(properties);

        logConfig(onEvent, monitorsFormat, root);
        return decorateCache(DiskCache
                .<MonitorReports>builder()
                .id(id)
                .root(root)
                .format(decorateFormat(monitorsFormat, isNoCompression(properties)))
                .namePrefix("M")
                .clock(clock)
                .onEvent(onEvent)
                .onError(onError)
                .build());
    }

    @Override
    public @NonNull Cache<Credentials> getCredentialsCache(@NonNull WebSource source, @Nullable EventListener onEvent, @Nullable ErrorListener onError) {
        return Cache.noOp();
    }

    @Override
    public @NonNull Collection<String> getFileCachingProperties() {
        return emptyList();
    }

    @Override
    public @NonNull Collection<String> getWebCachingProperties() {
        return emptyList();
    }

    private <T extends HasPersistence> FileFormat<T> decorateFormat(FileFormat<T> format, boolean noCompression) {
        return lock(noCompression ? wrap(format) : gzip(wrap(format)));
    }

    private <T extends HasExpiration> Cache<T> decorateCache(Cache<T> delegate) {
        return dry(new LockingByKeyCache<>(delegate));
    }

    private static <V extends HasExpiration> Cache<V> dry(Cache<V> cache) {
        return DualCache
                .<V>builder()
                .first(MemCache.<V>builder().clock(cache.getClock()).build())
                .second(cache)
                .clock(cache.getClock())
                .build();
    }

    private void logConfig(EventListener onEvent, FileFormat<?> format, Path root) {
        if (onEvent != null)
            onEvent.accept(id, "Using cache folder " + root.toUri() + " with format '" + format.getFileExtension() + "'");
    }

    private static Path getCacheFolder(Function<? super String, ? extends CharSequence> properties) {
        File root = CACHE_FOLDER_PROPERTY.get(properties);
        return (root == null) ? DiskCache.SDMXDL_TMP_DIR : root.toPath();
    }

    private static boolean isNoCompression(Function<? super String, ? extends CharSequence> properties) {
        return NO_COMPRESSION_PROPERTY.get(properties);
    }
}
