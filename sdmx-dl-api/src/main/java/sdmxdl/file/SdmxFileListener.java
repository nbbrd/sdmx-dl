package sdmxdl.file;

import internal.sdmxdl.FunctionalListener;
import internal.sdmxdl.LoggingListener;
import internal.sdmxdl.NoOpListener;
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
        return LoggingListener.INSTANCE;
    }

    @StaticFactoryMethod
    static @NonNull SdmxFileListener noOp() {
        return NoOpListener.INSTANCE;
    }

    @StaticFactoryMethod
    static @NonNull SdmxFileListener of(@NonNull BiConsumer<? super SdmxFileSource, ? super String> listener) {
        return FunctionalListener.builder().onFile(listener).build();
    }
}
