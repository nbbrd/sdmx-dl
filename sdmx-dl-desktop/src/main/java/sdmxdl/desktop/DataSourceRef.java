package sdmxdl.desktop;

import lombok.NonNull;
import nbbrd.io.sys.SystemProperties;
import sdmxdl.*;
import sdmxdl.provider.ri.caching.RiCaching;
import sdmxdl.provider.ri.drivers.RiHttpUtils;
import sdmxdl.provider.ri.networking.RiNetworking;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class DataSourceRef {

    @NonNull
    String source;

    @lombok.Builder.Default
    @NonNull
    CatalogRef catalog = CatalogRef.NO_CATALOG;

    @lombok.Builder.Default
    @NonNull
    String flow = "";

    @lombok.Singular
    @NonNull
    List<String> dimensions;

    @lombok.Builder.Default
    @NonNull
    Languages languages = Sdmxdl.INSTANCE.getLanguages();

    @lombok.Singular
    Map<String, String> properties;

    @lombok.Builder.Default
    boolean debug = false;

    @lombok.Builder.Default
    @NonNull
    Toggle curlBackend = Toggle.DEFAULT;

    public FlowRef toFlowRef() {
        return FlowRef.parse(flow);
    }

    public WebSource toWebSource(SdmxWebManager manager) {
        WebSource result = manager.getSources().get(source);
        if (result == null) return null;
        WebSource.Builder builder = result.toBuilder().properties(properties);
        if (debug) {
            Path tmp = requireNonNull(SystemProperties.DEFAULT.getJavaIoTmpdir()).resolve(About.NAME).resolve("debug_" + source);
            builder.property(RiHttpUtils.DUMP_FOLDER_PROPERTY.getKey(), tmp.resolve("dump").toString());
            builder.property(RiCaching.CACHE_FOLDER_PROPERTY.getKey(), tmp.resolve("cache").toString());
            builder.property(RiCaching.NO_COMPRESSION_PROPERTY.getKey(), "true");
            builder.property(RiCaching.PERSISTENCE_ID_PROPERTY.getKey(), "JSON");
        }
        if (curlBackend != Toggle.DEFAULT) {
            builder.property(RiNetworking.URL_BACKEND_PROPERTY.getKey(), curlBackend.equals(Toggle.ENABLE) ? "CURL" : "JDK");
        }
        return builder.build();
    }

    public Connection getConnection(SdmxWebManager manager) throws IOException {
        return manager.getConnection(toWebSource(manager), languages);
    }
}
