package sdmxdl.web;

import internal.sdmxdl.FunctionalListener;
import internal.sdmxdl.LoggingListener;
import internal.sdmxdl.NoOpListener;
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
        return LoggingListener.INSTANCE;
    }

    @StaticFactoryMethod
    static @NonNull SdmxWebListener noOp() {
        return NoOpListener.INSTANCE;
    }

    @StaticFactoryMethod
    static @NonNull SdmxWebListener of(@NonNull BiConsumer<? super SdmxWebSource, ? super String> listener) {
        return FunctionalListener.builder().onWeb(listener).build();
    }
}
