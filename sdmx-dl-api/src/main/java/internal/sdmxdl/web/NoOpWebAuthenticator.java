package internal.sdmxdl.web;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.web.SdmxWebAuthenticator;
import sdmxdl.web.SdmxWebSource;

import java.net.PasswordAuthentication;
import java.util.Objects;

public enum NoOpWebAuthenticator implements SdmxWebAuthenticator {

    INSTANCE;

    @Override
    public @Nullable PasswordAuthentication getPasswordAuthentication(@NonNull SdmxWebSource source) {
        Objects.requireNonNull(source);
        return null;
    }
}
