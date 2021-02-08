package internal.sdmxdl.web;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebSource;

import java.util.function.BiConsumer;

@lombok.AllArgsConstructor
public final class FunctionalSdmxWebListener implements SdmxWebListener {

    @lombok.NonNull
    private final BiConsumer<? super SdmxWebSource, ? super String> listener;

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void onSourceEvent(@NonNull SdmxWebSource source, @NonNull String message) {
        listener.accept(source, message);
    }
}
