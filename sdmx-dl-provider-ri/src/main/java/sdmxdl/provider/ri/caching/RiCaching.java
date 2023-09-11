package sdmxdl.provider.ri.caching;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.HasExpiration;
import sdmxdl.ext.Cache;
import sdmxdl.file.FileSource;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.format.DiskCache;
import sdmxdl.format.DiskCachingSupport;
import sdmxdl.format.MemCache;
import sdmxdl.format.spi.Persistence;
import sdmxdl.format.spi.PersistenceLoader;
import sdmxdl.provider.PropertiesSupport;
import sdmxdl.provider.Slow;
import sdmxdl.provider.ext.DualCache;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebCaching;

import java.io.File;
import java.util.Collection;
import java.util.function.Function;

import static nbbrd.io.text.BaseProperty.keysOf;

@DirectImpl
@ServiceProvider(FileCaching.class)
@ServiceProvider(WebCaching.class)
public final class RiCaching implements FileCaching, WebCaching {

    public static final BooleanProperty NO_CACHE_PROPERTY
            = BooleanProperty.of("sdmxdl.caching.noCache", false);

    public static final Property<File> CACHE_FOLDER
            = Property.of("sdmxdl.caching.cacheFolder", null, Parser.onFile(), Formatter.onFile());

    @lombok.Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final DiskCachingSupport lazyDelegate = initLazyDelegate();

    @Slow
    private DiskCachingSupport initLazyDelegate() {
        return DiskCachingSupport
                .builder()
                .id("RI_CACHING")
                .rank(100)
                .persistence(PersistenceLoader.load()
                        .stream()
                        .findFirst()
                        .orElseGet(Persistence::noOp))
                .build();
    }

    private DiskCachingSupport withRoot(File root) {
        return getLazyDelegate()
                .toBuilder()
                .root(root.toPath())
                .build();
    }

    private <V extends HasExpiration> Cache<V> dry(Cache<V> cache) {
        return new DualCache<>(
                MemCache.<V>builder().clock(cache.getClock()).build(),
                cache,
                cache.getClock()
        );
    }

    @Override
    public @NonNull String getWebCachingId() {
        return getLazyDelegate().getWebCachingId();
    }

    @Override
    public int getWebCachingRank() {
        return getLazyDelegate().getWebCachingRank();
    }

    @Override
    public @NonNull Cache<DataRepository> getDriverCache(
            @NonNull WebSource source,
            @Nullable EventListener<? super WebSource> onEvent,
            @Nullable ErrorListener<? super WebSource> onError) {

        Function<? super String, ? extends CharSequence> function = PropertiesSupport.asFunction(source);

        if (NO_CACHE_PROPERTY.get(function)) {
            if (onEvent != null) onEvent.accept(source, getWebCachingId(), "Cache disabled");
            return Cache.noOp();
        }

        File root = CACHE_FOLDER.get(function);
        if (root == null) root = DiskCache.SDMXDL_TMP_DIR.toFile();
        if (onEvent != null) onEvent.accept(source, getWebCachingId(), "Using cache folder '" + root + "'");

        return dry(withRoot(root).getDriverCache(source, onEvent, onError));
    }

    @Override
    public @NonNull Cache<MonitorReports> getMonitorCache(
            @NonNull WebSource source,
            @Nullable EventListener<? super WebSource> onEvent,
            @Nullable ErrorListener<? super WebSource> onError) {

        Function<? super String, ? extends CharSequence> function = PropertiesSupport.asFunction(source);

        if (NO_CACHE_PROPERTY.get(function)) {
            if (onEvent != null) onEvent.accept(source, getWebCachingId(), "Cache disabled");
            return Cache.noOp();
        }

        File root = CACHE_FOLDER.get(function);
        if (root == null) root = DiskCache.SDMXDL_TMP_DIR.toFile();
        if (onEvent != null) onEvent.accept(source, getWebCachingId(), "Using cache folder '" + root + "'");

        return dry(withRoot(root).getMonitorCache(source, onEvent, onError));
    }

    @Override
    public @NonNull Collection<String> getWebCachingProperties() {
        return keysOf(NO_CACHE_PROPERTY, CACHE_FOLDER);
    }

    @Override
    public @NonNull String getFileCachingId() {
        return getLazyDelegate().getFileCachingId();
    }

    @Override
    public int getFileCachingRank() {
        return getLazyDelegate().getFileCachingRank();
    }

    @Override
    public @NonNull Cache<DataRepository> getReaderCache(
            @NonNull FileSource source,
            @Nullable EventListener<? super FileSource> onEvent,
            @Nullable ErrorListener<? super FileSource> onError) {

        Function<? super String, ? extends CharSequence> function = PropertiesSupport.asFunction(source);

        if (NO_CACHE_PROPERTY.get(function)) {
            if (onEvent != null) onEvent.accept(source, getWebCachingId(), "Cache disabled");
            return Cache.noOp();
        }

        File root = CACHE_FOLDER.get(function);
        if (root == null) root = DiskCache.SDMXDL_TMP_DIR.toFile();
        if (onEvent != null) onEvent.accept(source, getWebCachingId(), "Using cache folder '" + root + "'");

        return dry(withRoot(root).getReaderCache(source, onEvent, onError));
    }

    @Override
    public @NonNull Collection<String> getFileCachingProperties() {
        return keysOf(NO_CACHE_PROPERTY, CACHE_FOLDER);
    }
}
