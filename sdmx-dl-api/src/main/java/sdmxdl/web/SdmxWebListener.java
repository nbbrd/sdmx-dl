package sdmxdl.web;

import internal.sdmxdl.web.DefaultSdmxWebListener;
import nbbrd.design.ThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;

@ThreadSafe
public interface SdmxWebListener {

    boolean isEnabled();

    void onSourceEvent(@NonNull SdmxWebSource source, @NonNull String message);

    @NonNull
    static SdmxWebListener getDefault() {
        return DefaultSdmxWebListener.INSTANCE;
    }
}
