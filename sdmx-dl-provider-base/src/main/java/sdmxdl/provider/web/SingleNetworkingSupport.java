package sdmxdl.provider.web;

import lombok.AccessLevel;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.format.design.ServiceSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Network;
import sdmxdl.web.spi.Networking;
import sdmxdl.web.spi.SSLFactory;
import sdmxdl.web.spi.URLConnectionFactory;

import java.net.ProxySelector;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.function.Supplier;

@ServiceSupport
@lombok.Builder(toBuilder = true)
public final class SingleNetworkingSupport implements Networking {

    private final @NonNull String id;

    @lombok.Builder.Default
    private final int rank = UNKNOWN_NETWORKING_RANK;

    @lombok.Builder.Default
    private final @NonNull Predicate<Properties> availability = ignore -> true;

    @lombok.Builder.Default
    private final @NonNull Supplier<? extends ProxySelector> proxySelector = Network.getDefault()::getProxySelector;

    @lombok.Builder.Default
    private final @NonNull Supplier<? extends SSLFactory> sslFactory = Network.getDefault()::getSSLFactory;

    @lombok.Builder.Default
    private final @NonNull Supplier<? extends URLConnectionFactory> urlConnectionFactory = Network.getDefault()::getURLConnectionFactory;

    @lombok.Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final @NonNull Network singleNetwork = initLazySingleNetwork();

    @Override
    public @NonNull String getNetworkingId() {
        return id;
    }

    @Override
    public int getNetworkingRank() {
        return rank;
    }

    @Override
    public boolean isNetworkingAvailable() {
        return availability.test(System.getProperties());
    }

    @Override
    public @NonNull Collection<String> getNetworkingProperties() {
        return Collections.emptyList();
    }

    @Override
    public @NonNull Network getNetwork(
            @NonNull WebSource source,
            @Nullable EventListener<? super WebSource> onEvent,
            @Nullable ErrorListener<? super WebSource> onError) {
        return getSingleNetwork();
    }

    private Network initLazySingleNetwork() {
        return LazyNetwork
                .builder()
                .proxySelector(proxySelector)
                .sslFactory(sslFactory)
                .urlConnectionFactory(urlConnectionFactory)
                .build();
    }

    public static final class Builder {

        public @NonNull Builder urlConnectionFactoryOf(@NonNull URLConnectionFactory supplier) {
            return urlConnectionFactory(() -> supplier);
        }
    }
}
