package internal.sdmxdl;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.file.SdmxFileListener;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebSource;

import java.util.Objects;

@lombok.extern.java.Log
public enum NoOpListener implements SdmxWebListener, SdmxFileListener {

    INSTANCE;

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void onWebSourceEvent(@NonNull SdmxWebSource source, @NonNull String message) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(message);
    }

    @Override
    public void onFileSourceEvent(@NonNull SdmxFileSource source, @NonNull String message) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(message);
    }
}
