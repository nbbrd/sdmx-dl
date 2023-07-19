package sdmxdl.provider.ri.web.networking;

import lombok.NonNull;
import nbbrd.io.text.BooleanProperty;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.provider.PropertiesSupport;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.Network;
import sdmxdl.web.spi.Networking;

import java.util.Collection;
import java.util.function.Function;

import static nbbrd.io.text.BaseProperty.keysOf;

@ServiceProvider
public final class RiNetworking implements Networking {

    public static final BooleanProperty AUTO_PROXY_PROPERTY
            = BooleanProperty.of("sdmxdl.networking.autoProxy", false);

    public static final BooleanProperty NO_SYSTEM_SSL_PROPERTY
            = BooleanProperty.of("sdmxdl.networking.noSystemSSL", false);

    public static final BooleanProperty NO_DEFAULT_SSL_PROPERTY
            = BooleanProperty.of("sdmxdl.networking.noDefaultSSL", false);

    public static final BooleanProperty CURL_BACKEND_PROPERTY
            = BooleanProperty.of("sdmxdl.networking.curlBackend", false);

    @Override
    public @NonNull String getNetworkingId() {
        return "RI_NETWORKING";
    }

    @Override
    public int getNetworkingRank() {
        return 100;
    }

    @Override
    public boolean isNetworkingAvailable() {
        return true;
    }

    @Override
    public @NonNull Collection<String> getNetworkingProperties() {
        return keysOf(
                AUTO_PROXY_PROPERTY,
                NO_SYSTEM_SSL_PROPERTY,
                NO_DEFAULT_SSL_PROPERTY,
                CURL_BACKEND_PROPERTY
        );
    }

    @Override
    public @NonNull Network getNetwork(
            @NonNull SdmxWebSource source,
            @Nullable EventListener<? super SdmxWebSource> onEvent,
            @Nullable ErrorListener<? super SdmxWebSource> onError) {
        RiNetwork result = getNetwork(PropertiesSupport.asFunction(source));
        if (onEvent != null) onEvent.accept(source, getNetworkingId(), "Using " + result);
        return result;
    }

    private static RiNetwork getNetwork(Function<? super String, ? extends CharSequence> properties) {
        return RiNetwork
                .builder()
                .autoProxy(AUTO_PROXY_PROPERTY.get(properties))
                .noDefaultSSL(NO_DEFAULT_SSL_PROPERTY.get(properties))
                .noSystemSSL(NO_SYSTEM_SSL_PROPERTY.get(properties))
                .curlBackend(CURL_BACKEND_PROPERTY.get(properties))
                .build();
    }
}
