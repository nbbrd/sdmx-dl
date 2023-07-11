package sdmxdl.web.spi;

import internal.sdmxdl.web.spi.DefaultNetworking;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.ThreadSafe;
import nbbrd.service.*;
import sdmxdl.web.SdmxWebSource;

import java.util.Collection;

@ServiceDefinition(
        quantifier = Quantifier.SINGLE,
        loaderName = "internal.util.NetworkingLoader",
        fallback = DefaultNetworking.class
)
@ThreadSafe
public interface Networking {

    @ServiceId
    @NonNull String getNetworkingId();

    @ServiceSorter(reverse = true)
    int getNetworkingRank();

    @ServiceFilter
    boolean isNetworkingAvailable();

    @NonNull Collection<String> getNetworkingProperties();

    @NonNull Network getNetwork(@NonNull SdmxWebSource source);

    int UNKNOWN_NETWORKING_RANK = -1;

    @StaticFactoryMethod
    static @NonNull Networking getDefault() {
        return DefaultNetworking.INSTANCE;
    }
}
