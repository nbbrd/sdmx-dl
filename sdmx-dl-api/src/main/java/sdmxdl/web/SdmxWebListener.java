package sdmxdl.web;

import internal.sdmxdl.web.DefaultSdmxWebListener;
import internal.sdmxdl.web.FunctionalSdmxWebListener;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.ThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.BiConsumer;

@ThreadSafe
public interface SdmxWebListener {

    boolean isEnabled();

    void onSourceEvent(@NonNull SdmxWebSource source, @NonNull String message);

    @StaticFactoryMethod
    static @NonNull SdmxWebListener getDefault() {
        return DefaultSdmxWebListener.LOG_TO_INFO;
    }

    @StaticFactoryMethod
    static @NonNull SdmxWebListener of(@NonNull BiConsumer<? super SdmxWebSource, ? super String> listener) {
        return new FunctionalSdmxWebListener(listener);
    }
}
