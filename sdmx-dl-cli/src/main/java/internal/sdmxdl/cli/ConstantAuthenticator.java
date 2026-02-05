package internal.sdmxdl.cli;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Authenticator;

import java.net.PasswordAuthentication;
import java.util.Collection;
import java.util.Collections;

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
    public @Nullable PasswordAuthentication getPasswordAuthenticationOrNull(@NonNull WebSource source,
                                                                            @Nullable EventListener onEvent,
                                                                            @Nullable ErrorListener onError) {
        return user;
    }

    @Override
    public void invalidateAuthentication(@NonNull WebSource source,
                                         @Nullable EventListener onEvent,
                                         @Nullable ErrorListener onError) {
    }

    @Override
    public @NonNull Collection<String> getAuthenticatorProperties() {
        return Collections.emptyList();
    }
}
