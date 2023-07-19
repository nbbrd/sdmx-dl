package internal.sdmxdl.web.spi;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.web.SdmxWebSource;
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
    public @NonNull Network getNetwork(
            @NonNull SdmxWebSource source,
            @Nullable EventListener<? super SdmxWebSource> onEvent,
            @Nullable ErrorListener<? super SdmxWebSource> onError) {
        return Network.getDefault();
    }
}
