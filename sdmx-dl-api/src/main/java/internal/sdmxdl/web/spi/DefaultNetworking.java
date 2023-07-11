package internal.sdmxdl.web.spi;

import lombok.NonNull;
import sdmxdl.web.spi.Network;
import sdmxdl.web.SdmxWebSource;
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
    public @NonNull Network getNetwork(@NonNull SdmxWebSource source) {
        return Network.getDefault();
    }
}
