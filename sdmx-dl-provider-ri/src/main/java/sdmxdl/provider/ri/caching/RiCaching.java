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
import sdmxdl.file.FileSource;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.provider.PropertiesSupport;
import sdmxdl.provider.caching.DiskCachingSupport;
import sdmxdl.provider.caching.MemCache;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.format.kryo.KryoFileFormat;
import sdmxdl.web.Credentials;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebCaching;

import java.time.Duration;
import java.util.Collection;
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

    // Set max confidentiality
    @PropertyDefinition
    public static final Property<Confidentiality> MAX_CONFIDENTIALITY_PROPERTY
            = Property.of("sdmxdl.caching.maxConfidentiality", Confidentiality.RESTRICTED, Parser.onEnum(Confidentiality.class), Formatter.onEnum());

    private static final String ID = "RI_CACHING";

    private static final int RANK = 100;

    private static final Collection<String> PROPERTIES = keysOf(
            NO_CACHE_PROPERTY,
            DiskCachingSupport.CACHE_FOLDER_PROPERTY,
            DiskCachingSupport.NO_COMPRESSION_PROPERTY,
            MAX_CONFIDENTIALITY_PROPERTY
    );

    private final DiskCachingSupport diskCaching = DiskCachingSupport
            .builder()
            .id(ID)
            .rank(RANK)
            .repositoryFormat(KryoFileFormat.of(DataRepository.class))
            .monitorsFormat(KryoFileFormat.of(MonitorReports.class))
            .build();

    private final VaultCachingSupport vaultCaching = VaultCachingSupport
            .builder()
            .id(ID)
            .build();

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
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError) {

        Function<? super String, ? extends CharSequence> properties = PropertiesSupport.asFunction(source);

        if (isNoCache(properties)) {
            return noCache(onEvent);
        }

        if (isForbidden(properties, source.getConfidentiality())) {
            return forbiddenCache(onEvent);
        }

        return diskCaching
                .getDriverCache(source, onEvent, onError);
    }

    @Override
    public @NonNull Cache<MonitorReports> getMonitorCache(
            @NonNull WebSource source,
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError) {

        Function<? super String, ? extends CharSequence> properties = PropertiesSupport.asFunction(source);

        if (isNoCache(properties)) {
            return noCache(onEvent);
        }

        if (isForbidden(properties, source.getConfidentiality())) {
            return forbiddenCache(onEvent);
        }

        return diskCaching
                .getMonitorCache(source, onEvent, onError);
    }

    @Override
    public @NonNull Cache<Credentials> getCredentialsCache(
            @NonNull WebSource source,
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError) {

        return vaultCaching
                .getCredentialsCache(Duration.ofMinutes(5), onEvent, onError);
    }

    @Override
    public @NonNull Cache<DataRepository> getReaderCache(
            @NonNull FileSource source,
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError) {

        Function<? super String, ? extends CharSequence> properties = PropertiesSupport.asFunction(source);

        if (isNoCache(properties)) {
            return noCache(onEvent);
        }

        return diskCaching
                .getReaderCache(source, onEvent, onError);
    }

    private <V extends HasExpiration> Cache<V> noCache(EventListener onEvent) {
        if (onEvent != null) onEvent.accept(ID, "Cache disabled");
        return Cache.noOp();
    }

    private static <V extends HasExpiration> Cache<V> forbiddenCache(EventListener onEvent) {
        if (onEvent != null) onEvent.accept(ID, "Cache forbidden");
        return MemCache.<V>builder().build();
    }

    private static boolean isNoCache(Function<? super String, ? extends CharSequence> properties) {
        return NO_CACHE_PROPERTY.get(properties);
    }

    @VisibleForTesting
    static boolean isForbidden(Function<? super String, ? extends CharSequence> properties, Confidentiality confidentiality) {
        return confidentiality.compareTo(requireNonNull(MAX_CONFIDENTIALITY_PROPERTY.get(properties))) > 0;
    }
}
