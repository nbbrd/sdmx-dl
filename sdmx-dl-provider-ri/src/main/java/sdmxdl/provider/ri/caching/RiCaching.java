package sdmxdl.provider.ri.caching;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;
import sdmxdl.*;
import sdmxdl.ext.Cache;
import sdmxdl.ext.Persistence;
import sdmxdl.file.FileSource;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.format.DiskCache;
import sdmxdl.format.DiskCachingSupport;
import sdmxdl.format.MemCache;
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

import static java.util.Objects.requireNonNull;
import static nbbrd.io.text.BaseProperty.keysOf;

@DirectImpl
@ServiceProvider(FileCaching.class)
@ServiceProvider(WebCaching.class)
public final class RiCaching implements FileCaching, WebCaching {

    // Disable caching
    @PropertyDefinition
    public static final BooleanProperty NO_CACHE_PROPERTY
            = BooleanProperty.of("sdmxdl.caching.noCache", false);

    // Set cache folder
    @PropertyDefinition
    public static final Property<File> CACHE_FOLDER_PROPERTY
            = Property.of("sdmxdl.caching.cacheFolder", null, Parser.onFile(), Formatter.onFile());

    // Disable cache compression
    @PropertyDefinition
    public static final BooleanProperty NO_COMPRESSION_PROPERTY
            = BooleanProperty.of("sdmxdl.caching.noCompression", false);

    // Set cache persistence backend
    @PropertyDefinition
    public static final Property<String> PERSISTENCE_ID_PROPERTY
            = Property.of("sdmxdl.caching.persistenceId", null, Parser.onString(), Formatter.onString());

    // Set max confidentiality
    @PropertyDefinition
    public static final Property<Confidentiality> MAX_CONFIDENTIALITY_PROPERTY
            = Property.of("sdmxdl.caching.maxConfidentiality", Confidentiality.RESTRICTED, Parser.onEnum(Confidentiality.class), Formatter.onEnum());

    private static final String ID = "RI_CACHING";

    private static final int RANK = 100;

    private static final Collection<String> PROPERTIES = keysOf(
            NO_CACHE_PROPERTY,
            CACHE_FOLDER_PROPERTY,
            NO_COMPRESSION_PROPERTY,
            PERSISTENCE_ID_PROPERTY,
            MAX_CONFIDENTIALITY_PROPERTY
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
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError) {

        Function<? super String, ? extends CharSequence> properties = PropertiesSupport.asFunction(source);

        if (isNoCache(properties)) {
            return noCache(onEvent);
        }

        if (isForbidden(properties, source.getConfidentiality())) {
            return forbiddenCache(onEvent);
        }

        return getDiskCaching(properties)
                .getDriverCache(source, persistences, onEvent, onError);
    }

    @Override
    public @NonNull Cache<MonitorReports> getMonitorCache(
            @NonNull WebSource source,
            @NonNull List<Persistence> persistences,
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError) {

        Function<? super String, ? extends CharSequence> properties = PropertiesSupport.asFunction(source);

        if (isNoCache(properties)) {
            return noCache(onEvent);
        }

        if (isForbidden(properties, source.getConfidentiality())) {
            return forbiddenCache(onEvent);
        }

        return getDiskCaching(properties)
                .getMonitorCache(source, persistences, onEvent, onError);
    }

    @Override
    public @NonNull Cache<DataRepository> getReaderCache(
            @NonNull FileSource source,
            @NonNull List<Persistence> persistences,
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError) {

        Function<? super String, ? extends CharSequence> properties = PropertiesSupport.asFunction(source);

        if (isNoCache(properties)) {
            return noCache(onEvent);
        }

        return getDiskCaching(properties)
                .getReaderCache(source, persistences, onEvent, onError);
    }

    private <V extends HasExpiration> Cache<V> noCache(EventListener onEvent) {
        if (onEvent != null) onEvent.accept(ID, "Cache disabled");
        return Cache.noOp();
    }

    private static <V extends HasExpiration> Cache<V> forbiddenCache(EventListener onEvent) {
        if (onEvent != null) onEvent.accept(ID, "Cache forbidden");
        return MemCache.<V>builder().build();
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

    @VisibleForTesting
    static boolean isForbidden(Function<? super String, ? extends CharSequence> properties, Confidentiality confidentiality) {
        return confidentiality.compareTo(requireNonNull(MAX_CONFIDENTIALITY_PROPERTY.get(properties))) > 0;
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
