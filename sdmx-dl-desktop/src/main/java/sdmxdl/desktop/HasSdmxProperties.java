package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxManager;

public interface HasSdmxProperties<M extends SdmxManager<?>> {

    String SDMX_MANAGER_PROPERTY = "sdmxManager";

    @NonNull M getSdmxManager();

    void setSdmxManager(@NonNull M sdmxManager);

    String LANGUAGES_PROPERTY = "languages";

    @NonNull LanguagePriorityList getLanguages();

    void setLanguages(@NonNull LanguagePriorityList languages);
}
