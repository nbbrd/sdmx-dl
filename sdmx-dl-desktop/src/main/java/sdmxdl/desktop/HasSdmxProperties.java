package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.SdmxManager;
import sdmxdl.ext.Registry;

public interface HasSdmxProperties<M extends SdmxManager<?>> {

    String SDMX_MANAGER_PROPERTY = "sdmxManager";

    @NonNull M getSdmxManager();

    void setSdmxManager(@NonNull M sdmxManager);

    String REGISTRY_PROPERTY = "registry";

    @NonNull Registry getRegistry();

    void setRegistry(@NonNull Registry registry);

    Registry NO_OP_REGISTRY = Registry.builder().build();
}
