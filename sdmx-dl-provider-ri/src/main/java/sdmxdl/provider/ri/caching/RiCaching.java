package sdmxdl.provider.ri.caching;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
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
import sdmxdl.file.spi.FileCaching;
import sdmxdl.format.DiskCache;
import sdmxdl.format.DiskCachingSupport;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.provider.PropertiesSupport;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebCaching;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static nbbrd.io.text.BaseProperty.keysOf;

@DirectImpl
@ServiceProvider(FileCaching.class)
@ServiceProvider(WebCaching.class)
public final class RiCaching implements FileCaching, WebCaching {

    @PropertyDefinition
    public static final BooleanProperty NO_CACHE_PROPERTY
            = BooleanProperty.of("sdmxdl.caching.noCache", false);

    @PropertyDefinition
    public static final Property<File> CACHE_FOLDER_PROPERTY
            = Property.of("sdmxdl.caching.cacheFolder", null, Parser.onFile(), Formatter.onFile());

    @PropertyDefinition
    public static final BooleanProperty NO_COMPRESSION_PROPERTY
            = BooleanProperty.of("sdmxdl.caching.noCompression", false);

    @PropertyDefinition
    public static final Property<String> PERSISTENCE_ID_PROPERTY
            = Property.of("sdmxdl.caching.persistenceId", null, Parser.onString(), Formatter.onString());

    private static final String ID = "RI_CACHING";

    private static final int RANK = 100;

    private static final Collection<String> PROPERTIES = keysOf(
            NO_CACHE_PROPERTY,
            CACHE_FOLDER_PROPERTY,
            NO_COMPRESSION_PROPERTY,
            PERSISTENCE_ID_PROPERTY
    );

    @Override
    public @NonNull String getWebCachingId() {
        return ID;
    }

    @Override
    public @NonNull String getFileCachingId() {
        return ID;
    }

    @Override
    public int getWebCachingRank() {
        return RANK;
    }

    @Override
    public int getFileCachingRank() {
        return RANK;
    }

    @Override
    public @NonNull Collection<String> getWebCachingProperties() {
        return PROPERTIES;
    }

    @Override
    public @NonNull Collection<String> getFileCachingProperties() {
        return PROPERTIES;
    }

    @Override
    public @NonNull Cache<DataRepository> getDriverCache(
            @NonNull WebSource source,
            @NonNull List<Persistence> persistences,
            @Nullable EventListener<? super WebSource> onEvent,
            @Nullable ErrorListener<? super WebSource> onError) {

        Function<? super String, ? extends CharSequence> properties = PropertiesSupport.asFunction(source);

        if (isNoCache(properties)) {
            return noCache(source, onEvent);
        }

        return getDiskCaching(properties)
                .getDriverCache(source, persistences, onEvent, onError);
    }

    @Override
    public @NonNull Cache<MonitorReports> getMonitorCache(
            @NonNull WebSource source,
            @NonNull List<Persistence> persistences,
            @Nullable EventListener<? super WebSource> onEvent,
            @Nullable ErrorListener<? super WebSource> onError) {

        Function<? super String, ? extends CharSequence> properties = PropertiesSupport.asFunction(source);

        if (isNoCache(properties)) {
            return noCache(source, onEvent);
        }

        return getDiskCaching(properties)
                .getMonitorCache(source, persistences, onEvent, onError);
    }

    @Override
    public @NonNull Cache<DataRepository> getReaderCache(
            @NonNull FileSource source,
            @NonNull List<Persistence> persistences,
            @Nullable EventListener<? super FileSource> onEvent,
            @Nullable ErrorListener<? super FileSource> onError) {

        Function<? super String, ? extends CharSequence> properties = PropertiesSupport.asFunction(source);

        if (isNoCache(properties)) {
            return noCache(source, onEvent);
        }

        return getDiskCaching(properties)
                .getReaderCache(source, persistences, onEvent, onError);
    }

    private <V extends HasExpiration, T extends Source> Cache<V> noCache(T source, EventListener<? super T> onEvent) {
        if (onEvent != null) onEvent.accept(source, ID, "Cache disabled");
        return Cache.noOp();
    }

    private static DiskCachingSupport getDiskCaching(Function<? super String, ? extends CharSequence> properties) {
        return DiskCachingSupport
                .builder()
                .id(ID)
                .rank(RANK)
                .root(getCacheFolder(properties))
                .noCompression(isNoCompression(properties))
                .persistenceId(getPersistenceId(properties))
                .build();
    }

    private static boolean isNoCache(Function<? super String, ? extends CharSequence> properties) {
        return NO_CACHE_PROPERTY.get(properties);
    }

    private static Path getCacheFolder(Function<? super String, ? extends CharSequence> properties) {
        File root = CACHE_FOLDER_PROPERTY.get(properties);
        return (root == null) ? DiskCache.SDMXDL_TMP_DIR : root.toPath();
    }

    private static boolean isNoCompression(Function<? super String, ? extends CharSequence> properties) {
        return NO_COMPRESSION_PROPERTY.get(properties);
    }

    private static String getPersistenceId(Function<? super String, ? extends CharSequence> properties) {
        String result = PERSISTENCE_ID_PROPERTY.get(properties);
        return result != null ? result : "";
    }
}
