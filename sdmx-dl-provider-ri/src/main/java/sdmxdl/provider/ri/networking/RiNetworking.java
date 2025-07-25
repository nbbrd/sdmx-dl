package sdmxdl.provider.ri.networking;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.provider.PropertiesSupport;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Network;
import sdmxdl.web.spi.Networking;

import java.util.Collection;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static nbbrd.io.text.BaseProperty.keysOf;

@DirectImpl
@ServiceProvider
public final class RiNetworking implements Networking {

    // Enable automatic proxy detection
    @PropertyDefinition
    public static final BooleanProperty AUTO_PROXY_PROPERTY
            = BooleanProperty.of("sdmxdl.networking.autoProxy", false);

    // Disable system truststore
    @PropertyDefinition
    public static final BooleanProperty NO_SYSTEM_SSL_PROPERTY
            = BooleanProperty.of("sdmxdl.networking.noSystemSSL", false);

    // Disable default truststore
    @PropertyDefinition
    public static final BooleanProperty NO_DEFAULT_SSL_PROPERTY
            = BooleanProperty.of("sdmxdl.networking.noDefaultSSL", false);

    // Set networking URL backend
    @PropertyDefinition
    public static final Property<String> URL_BACKEND_PROPERTY
            = Property.of("sdmxdl.networking.urlBackend", RiNetwork.DEFAULT_URL_BACKEND, Parser.onString(), Formatter.onString());

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
                URL_BACKEND_PROPERTY
        );
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void warmupNetwork() {
        RiNetwork network = getNetwork(key -> PropertiesSupport.getProperty(emptyMap(), key));
        network.getSSLFactory().getLazyDelegate();
        network.getProxySelector();
        network.getURLConnectionFactory();
    }

    @Override
    public @NonNull Network getNetwork(
            @NonNull WebSource source,
            @Nullable EventListener<? super WebSource> onEvent,
            @Nullable ErrorListener<? super WebSource> onError) {
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
                .urlBackend(getUrlBackend(properties))
                .build();
    }

    private static String getUrlBackend(Function<? super String, ? extends CharSequence> properties) {
        String result = URL_BACKEND_PROPERTY.get(properties);
        return result != null ? result : RiNetwork.DEFAULT_URL_BACKEND;
    }
}
