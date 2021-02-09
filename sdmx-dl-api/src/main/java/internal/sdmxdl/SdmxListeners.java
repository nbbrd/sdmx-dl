package internal.sdmxdl;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.file.SdmxFileListener;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebSource;

import java.util.Objects;
import java.util.logging.Level;

@lombok.extern.java.Log
public enum SdmxListeners implements SdmxWebListener, SdmxFileListener {

    LOG_TO_INFO {
        @Override
        public boolean isEnabled() {
            return log.isLoggable(Level.INFO);
        }

        @Override
        public void onWebSourceEvent(SdmxWebSource source, String message) {
            Objects.requireNonNull(source);
            Objects.requireNonNull(message);
            log.log(Level.INFO, message);
        }

        @Override
        public void onFileSourceEvent(@NonNull SdmxFileSource source, @NonNull String message) {
            Objects.requireNonNull(source);
            Objects.requireNonNull(message);
            log.log(Level.INFO, message);
        }
    },
    NO_OP {
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
}
