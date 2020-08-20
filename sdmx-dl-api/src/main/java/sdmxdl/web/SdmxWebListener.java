package sdmxdl.web;

import internal.sdmxdl.web.DefaultSdmxWebListener;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface SdmxWebListener {

    boolean isEnabled();

    void onSourceEvent(@NonNull SdmxWebSource source, @NonNull String message);

    @NonNull
    static SdmxWebListener getDefault() {
        return DefaultSdmxWebListener.INSTANCE;
    }
}
