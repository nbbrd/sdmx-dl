package internal.sdmxdl.web.spi;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Network;
import sdmxdl.web.spi.Networking;

import java.util.Collection;
import java.util.Collections;

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
    public @NonNull Collection<String> getNetworkingProperties() {
        return Collections.emptyList();
    }

    @Override
    public void warmupNetwork() {
        Network network = Network.getDefault();
        network.getSSLFactory().getSSLSocketFactory();
        network.getSSLFactory().getHostnameVerifier();
        network.getProxySelector();
        network.getURLConnectionFactory();
    }

    @Override
    public @NonNull Network getNetwork(
            @NonNull WebSource source,
            @Nullable EventListener<? super WebSource> onEvent,
            @Nullable ErrorListener<? super WebSource> onError) {
        return Network.getDefault();
    }
}
