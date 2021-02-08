package sdmxdl.file;

import internal.sdmxdl.DefaultSdmxListener;
import internal.sdmxdl.FunctionalSdmxListener;
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
        return DefaultSdmxListener.LOG_TO_INFO;
    }

    @StaticFactoryMethod
    static @NonNull SdmxFileListener of(@NonNull BiConsumer<? super SdmxFileSource, ? super String> listener) {
        return FunctionalSdmxListener.builder().onFile(listener).build();
    }
}
