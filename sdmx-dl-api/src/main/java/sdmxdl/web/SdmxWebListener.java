package sdmxdl.web;

import internal.sdmxdl.DefaultSdmxListener;
import internal.sdmxdl.FunctionalSdmxListener;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.ThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.BiConsumer;

@ThreadSafe
public interface SdmxWebListener {

    boolean isEnabled();

    void onWebSourceEvent(@NonNull SdmxWebSource source, @NonNull String message);

    @StaticFactoryMethod
    static @NonNull SdmxWebListener getDefault() {
        return DefaultSdmxListener.LOG_TO_INFO;
    }

    @StaticFactoryMethod
    static @NonNull SdmxWebListener of(@NonNull BiConsumer<? super SdmxWebSource, ? super String> listener) {
        return FunctionalSdmxListener.builder().onWeb(listener).build();
    }
}
