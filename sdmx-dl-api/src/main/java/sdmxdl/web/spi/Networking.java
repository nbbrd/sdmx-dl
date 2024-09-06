package sdmxdl.web.spi;

import internal.sdmxdl.web.spi.DefaultNetworking;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.ThreadSafe;
import nbbrd.service.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.web.WebSource;

import java.util.Collection;

@ServiceDefinition(
        quantifier = Quantifier.SINGLE,
        fallback = DefaultNetworking.class,
        loaderName = "internal.{{canonicalName}}Loader"
)
@ThreadSafe
public interface Networking {

    @ServiceId(pattern = ServiceId.SCREAMING_SNAKE_CASE)
    @NonNull String getNetworkingId();

    @ServiceSorter(reverse = true)
    int getNetworkingRank();

    @ServiceFilter
    boolean isNetworkingAvailable();

    @NonNull Collection<String> getNetworkingProperties();

    @NonNull Network getNetwork(
            @NonNull WebSource source,
            @Nullable EventListener<? super WebSource> onEvent,
            @Nullable ErrorListener<? super WebSource> onError);

    int UNKNOWN_NETWORKING_RANK = -1;

    String NETWORKING_PROPERTY_PREFIX = "sdmxdl.networking";

    @StaticFactoryMethod
    static @NonNull Networking getDefault() {
        return DefaultNetworking.INSTANCE;
    }
}
