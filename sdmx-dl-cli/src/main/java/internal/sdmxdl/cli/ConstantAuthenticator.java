package internal.sdmxdl.cli;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.Authenticator;

import java.net.PasswordAuthentication;

@lombok.AllArgsConstructor
final class ConstantAuthenticator implements Authenticator {

    private final PasswordAuthentication user;

    @Override
    public @NonNull String getAuthenticatorId() {
        return "CONSTANT";
    }

    @Override
    public boolean isAuthenticatorAvailable() {
        return true;
    }

    @Override
    public @Nullable PasswordAuthentication getPasswordAuthenticationOrNull(@NonNull SdmxWebSource source) {
        return user;
    }

    @Override
    public void invalidateAuthentication(@NonNull SdmxWebSource source) {
    }
}
