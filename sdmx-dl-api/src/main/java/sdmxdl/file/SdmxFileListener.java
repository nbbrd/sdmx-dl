package sdmxdl.file;

import internal.sdmxdl.SdmxListeners;
import internal.sdmxdl.FunctionalListener;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.ThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.BiConsumer;

@ThreadSafe
public interface SdmxFileListener {

    boolean isEnabled();

    void onFileSourceEvent(@NonNull SdmxFileSource source, @NonNull String message);

    @StaticFactoryMethod
    static @NonNull SdmxFileListener getDefault() {
        return SdmxListeners.LOG_TO_INFO;
    }

    @StaticFactoryMethod
    static @NonNull SdmxFileListener noOp() {
        return SdmxListeners.NO_OP;
    }

    @StaticFactoryMethod
    static @NonNull SdmxFileListener of(@NonNull BiConsumer<? super SdmxFileSource, ? super String> listener) {
        return FunctionalListener.builder().onFile(listener).build();
    }
}
