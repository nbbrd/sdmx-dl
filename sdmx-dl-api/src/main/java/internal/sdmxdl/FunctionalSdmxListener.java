package internal.sdmxdl;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.file.SdmxFileListener;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebSource;

import java.util.function.BiConsumer;

@lombok.Builder
public final class FunctionalSdmxListener implements SdmxWebListener, SdmxFileListener {

    @lombok.NonNull
    @lombok.Builder.Default
    private final BiConsumer<? super SdmxWebSource, ? super String> onWeb = FunctionalSdmxListener::doNothing;

    @lombok.NonNull
    @lombok.Builder.Default
    private final BiConsumer<? super SdmxFileSource, ? super String> onFile = FunctionalSdmxListener::doNothing;

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void onWebSourceEvent(@NonNull SdmxWebSource source, @NonNull String message) {
        onWeb.accept(source, message);
    }

    @Override
    public void onFileSourceEvent(@NonNull SdmxFileSource source, @NonNull String message) {
        onFile.accept(source, message);
    }

    private static void doNothing(Object source, Object message) {
    }
}
