package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.SdmxManager;
import sdmxdl.ext.Registry;
import sdmxdl.web.SdmxWebManager;

public interface HasSdmxProperties<M extends SdmxManager<?>> {

    String SDMX_MANAGER_PROPERTY = "sdmxManager";

    @NonNull M getSdmxManager();

    void setSdmxManager(@NonNull M sdmxManager);

    SdmxWebManager NO_OP_SDMX_WEB_MANAGER = SdmxWebManager.builder().build();

    String REGISTRY_PROPERTY = "registry";

    @NonNull Registry getRegistry();

    void setRegistry(@NonNull Registry registry);

    Registry NO_OP_REGISTRY = Registry.builder().build();
}
