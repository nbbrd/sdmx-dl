package internal.sdmxdl.cli;

import lombok.NonNull;
import sdmxdl.ext.Persistence;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.spi.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@lombok.Value
public class Plugin {

    public enum Type {
        DRIVER,
        AUTHENTICATOR,
        MONITOR,
        PERSISTENCE,
        REGISTRY,
        WEB_CACHING,
        FILE_CACHING,
        NETWORKING
    }

    public static List<Plugin> allOf(SdmxWebManager manager) {
        List<Plugin> result = new ArrayList<>();
        manager.getDrivers().stream().map(Plugin::of).forEach(result::add);
        manager.getAuthenticators().stream().map(Plugin::of).forEach(result::add);
        manager.getMonitors().stream().map(Plugin::of).forEach(result::add);
        manager.getPersistences().stream().map(Plugin::of).forEach(result::add);
        result.add(Plugin.of(manager.getRegistry()));
        result.add(Plugin.of(manager.getCaching()));
        result.add(Plugin.of(manager.getNetworking()));
        return result;
    }

    static Plugin of(Driver o) {
        return new Plugin(Type.DRIVER, o.getDriverId(), o.getDriverProperties());
    }

    static Plugin of(Authenticator o) {
        return new Plugin(Type.AUTHENTICATOR, o.getAuthenticatorId(), o.getAuthenticatorProperties());
    }

    static Plugin of(Monitor o) {
        return new Plugin(Type.MONITOR, o.getMonitorId(), o.getMonitorProperties());
    }

    static Plugin of(Persistence o) {
        return new Plugin(Type.PERSISTENCE, o.getPersistenceId(), o.getPersistenceProperties());
    }

    static Plugin of(Registry o) {
        return new Plugin(Type.REGISTRY, o.getRegistryId(), o.getRegistryProperties());
    }

    static Plugin of(WebCaching o) {
        return new Plugin(Type.WEB_CACHING, o.getWebCachingId(), o.getWebCachingProperties());
    }

    static Plugin of(FileCaching o) {
        return new Plugin(Type.FILE_CACHING, o.getFileCachingId(), o.getFileCachingProperties());
    }

    static Plugin of(Networking o) {
        return new Plugin(Type.NETWORKING, o.getNetworkingId(), o.getNetworkingProperties());
    }

    @NonNull
    Type type;

    @NonNull
    String id;

    @NonNull
    Collection<String> Properties;
}
