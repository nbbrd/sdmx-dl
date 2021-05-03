package sdmxdl.web;

import internal.sdmxdl.FunctionalListener;
import internal.sdmxdl.SdmxListeners;
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
        return SdmxListeners.LOG_TO_INFO;
    }

    @StaticFactoryMethod
    static @NonNull SdmxWebListener noOp() {
        return SdmxListeners.NO_OP;
    }

    @StaticFactoryMethod
    static @NonNull SdmxWebListener of(@NonNull BiConsumer<? super SdmxWebSource, ? super String> listener) {
        return FunctionalListener.builder().onWeb(listener).build();
    }
}
