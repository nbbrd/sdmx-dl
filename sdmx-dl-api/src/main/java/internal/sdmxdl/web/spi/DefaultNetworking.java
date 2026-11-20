package internal.sdmxdl.web.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Network;
import sdmxdl.web.spi.Networking;

public enum DefaultNetworking implements Networking {
    INSTANCE;

    @Override
    public @NonNull String getNetworkingId() {
        return "DEFAULT";
    }

    @Override
    public int getNetworkingRank() {
        return UNKNOWN_NETWORKING_RANK;
    }

    @Override
    public boolean isNetworkingAvailable() {
        return true;
    }

    @Override
    public @NonNull Collection<String> getNetworkingPropertyNames() {
        return Collections.emptyList();
    }

    @Override
    public void warmupNetwork() {
        Network network = Network.getDefault();
        Objects.requireNonNull(network.getSSLFactory().getSSLSocketFactory());
        Objects.requireNonNull(network.getSSLFactory().getHostnameVerifier());
        Objects.requireNonNull(network.getProxySelector());
        Objects.requireNonNull(network.getURLConnectionFactory());
    }

    @Override
    public @NonNull Network getNetwork(
            @NonNull WebSource source, @Nullable EventListener onEvent, @Nullable ErrorListener onError) {
        return Network.getDefault();
    }
}
