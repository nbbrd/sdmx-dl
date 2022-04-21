package internal.sdmxdl.cli;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebAuthenticator;

import java.net.PasswordAuthentication;

@lombok.AllArgsConstructor
final class ConstantAuthenticator implements WebAuthenticator {

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
