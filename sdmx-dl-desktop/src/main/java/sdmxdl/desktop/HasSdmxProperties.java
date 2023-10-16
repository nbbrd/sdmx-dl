package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.Languages;
import sdmxdl.SdmxManager;

public interface HasSdmxProperties<M extends SdmxManager<?>> {

    String SDMX_MANAGER_PROPERTY = "sdmxManager";

    @NonNull M getSdmxManager();

    void setSdmxManager(@NonNull M sdmxManager);

    String LANGUAGES_PROPERTY = "languages";

    @NonNull Languages getLanguages();

    void setLanguages(@NonNull Languages languages);
}
