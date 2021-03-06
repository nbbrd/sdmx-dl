package internal.sdmxdl;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.file.SdmxFileListener;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebSource;

import java.util.Objects;
import java.util.function.BiConsumer;

@lombok.Builder
public final class FunctionalListener implements SdmxWebListener, SdmxFileListener {

    @lombok.NonNull
    @lombok.Builder.Default
    private final BiConsumer<? super SdmxWebSource, ? super String> onWeb = FunctionalListener::doNothing;

    @lombok.NonNull
    @lombok.Builder.Default
    private final BiConsumer<? super SdmxFileSource, ? super String> onFile = FunctionalListener::doNothing;

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void onWebSourceEvent(@NonNull SdmxWebSource source, @NonNull String message) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(message);
        onWeb.accept(source, message);
    }

    @Override
    public void onFileSourceEvent(@NonNull SdmxFileSource source, @NonNull String message) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(message);
        onFile.accept(source, message);
    }

    @SuppressWarnings("EmptyMethod")
    private static void doNothing(Object source, Object message) {
    }
}
