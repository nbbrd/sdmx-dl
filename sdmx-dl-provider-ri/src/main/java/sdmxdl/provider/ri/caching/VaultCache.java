package sdmxdl.provider.ri.caching;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;
import sdmxdl.provider.ri.spi.VaultService;
import sdmxdl.web.Credentials;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.time.Clock;
import java.time.Duration;

import static java.lang.String.format;
import static java.util.Locale.ROOT;

@lombok.Builder(toBuilder = true)
final class VaultCache implements Cache<Credentials> {

    private final @NonNull String id;

    @lombok.Builder.Default
    private final @NonNull Clock clock = Clock.systemDefaultZone();

    @lombok.Builder.Default
    private final @NonNull Duration ttl = Duration.ofMinutes(5);

    private final @Nullable EventListener onEvent;

    private final @Nullable ErrorListener onError;

    @lombok.Builder.Default
    private final @NonNull VaultService vault = VaultService.noOp();

    @Override
    public @NonNull Clock getClock() {
        return clock;
    }

    @Override
    public @Nullable Credentials get(@NonNull String key) {
        try {
            if (onEvent != null)
                onEvent.accept(id, format(ROOT, "Loading credentials from %s resource '%s'", vault.getVaultId(), key));
            String password = vault.loadPassword(key, key);
            if (password != null) {
                return Credentials.of(new PasswordAuthentication(key, password.toCharArray()), clock.instant().plus(ttl));
            }
            if (onEvent != null)
                onEvent.accept(id, format(ROOT, "Found no credentials in %s resource '%s'", vault.getVaultId(), key));
        } catch (IOException e) {
            if (onError != null)
                onError.accept(id, format(ROOT, "Failed to load credentials from %s resource '%s'", vault.getVaultId(), key), e);
        }
        return null;
    }

    @Override
    public void put(@NonNull String key, @Nullable Credentials value) {
        try {
            if (onEvent != null)
                onEvent.accept(id, format(ROOT, "Updating credentials to %s resource '%s'", vault.getVaultId(), key));
            vault.storePassword(key, key, value != null ? new String(value.getCredentials().getPassword()) : null);
        } catch (IOException e) {
            if (onError != null)
                onError.accept(id, format(ROOT, "Failed to update credentials to %s resource '%s'", vault.getVaultId(), key), e);
        }
    }
}
