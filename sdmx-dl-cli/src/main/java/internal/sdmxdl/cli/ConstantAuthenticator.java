package internal.sdmxdl.cli;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.web.spi.SdmxWebAuthenticator;
import sdmxdl.web.SdmxWebSource;

import java.net.PasswordAuthentication;

@lombok.AllArgsConstructor
final class ConstantAuthenticator implements SdmxWebAuthenticator {

    private final PasswordAuthentication user;

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public @Nullable PasswordAuthentication getPasswordAuthentication(@NonNull SdmxWebSource source) {
        return user;
    }

    @Override
    public void invalidate(@NonNull SdmxWebSource source) {
    }
}
