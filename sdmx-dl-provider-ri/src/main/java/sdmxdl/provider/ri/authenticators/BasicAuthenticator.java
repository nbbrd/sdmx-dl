package sdmxdl.provider.ri.authenticators;

import internal.sdmxdl.provider.ri.spi.PasswordPromptLoader;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;
import sdmxdl.provider.LockByKey;
import sdmxdl.provider.ri.drivers.AuthSchemes;
import sdmxdl.provider.ri.spi.PasswordPrompt;
import sdmxdl.provider.web.DriverProperties;
import sdmxdl.web.Credentials;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Authenticator;
import sdmxdl.web.spi.WebCaching;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;

@DirectImpl
@ServiceProvider
public final class BasicAuthenticator implements Authenticator {

    private final Duration ttl = Duration.ofMinutes(5);
    private final PasswordPrompt passwordPrompt = PasswordPromptLoader.load();

    @Override
    public @NonNull String getAuthenticatorId() {
        return "BASIC";
    }

    @Override
    public boolean isAuthenticatorAvailable() {
        return true;
    }

    @Override
    public PasswordAuthentication getPasswordAuthenticationOrNull(@NonNull WebSource source,
                                                                  @NonNull WebCaching caching,
                                                                  @Nullable EventListener onEvent,
                                                                  @Nullable ErrorListener onError) {
        if (isBasicAuthScheme(source)) {
            String resource = getResource(source);
            LockByKey lockByKey = new LockByKey();
            try {
                lockByKey.lock(resource);

                Cache<Credentials> cache = caching.getCredentialsCache(source, onEvent, onError);

                if (onEvent != null)
                    onEvent.accept(getAuthenticatorId(), "Acquiring username/password silently for resource '" + resource + "'");
                Credentials credentials = cache.get(resource);
                if (credentials != null) {
                    return toPasswordAuthentication(credentials);
                }

                if (onEvent != null)
                    onEvent.accept(getAuthenticatorId(), "Acquiring username/password interactively from '" + passwordPrompt.getPromptId() + "' for resource '" + resource + "'");
                PasswordAuthentication passwordAuthentication = passwordPrompt.promptCredentials(resource, "Enter your credentials for " + source.getId());
                if (passwordAuthentication != null) {
                    cache.put(resource, fromPasswordAuthentication(resource, passwordAuthentication, cache.getClock(), ttl));
                    return passwordAuthentication;
                }
            } catch (IOException ex) {
                if (onError != null)
                    onError.accept(getAuthenticatorId(), "Failed to acquire username/password for resource '" + resource + "'", ex);
            } finally {
                lockByKey.unlock(resource);
            }
        }
        return null;
    }

    @Override
    public void invalidateAuthentication(@NonNull WebSource source,
                                         @NonNull WebCaching caching,
                                         @Nullable EventListener onEvent,
                                         @Nullable ErrorListener onError) {
        if (isBasicAuthScheme(source)) {
            caching.getCredentialsCache(source, onEvent, onError).put(getResource(source), null);
        }
    }

    @Override
    public @NonNull Collection<String> getAuthenticatorPropertyNames() {
        return Collections.emptyList();
    }

    private static String getResource(WebSource source) {
        return "sdmx-dl:" + source.getEndpoint().getHost();
    }

    private static boolean isBasicAuthScheme(@NonNull WebSource source) {
        return AuthSchemes.BASIC_AUTH_SCHEME.equals(DriverProperties.AUTH_SCHEME_PROPERTY.get(source.getProperties()));
    }

    private static Credentials fromPasswordAuthentication(String resource, PasswordAuthentication passwordAuthentication, Clock clock, Duration ttl) {
        String secret = passwordAuthentication.getUserName() + ":" + new String(passwordAuthentication.getPassword());
        return Credentials.of(new PasswordAuthentication(resource, secret.toCharArray()), clock.instant().plus(ttl));
    }

    private static @Nullable PasswordAuthentication toPasswordAuthentication(Credentials credentials) {
        String secret = new String(credentials.getCredentials().getPassword());
        int separatorIndex = secret.indexOf(':');
        if (separatorIndex > 0) {
            return new PasswordAuthentication(
                    secret.substring(0, separatorIndex),
                    secret.substring(separatorIndex + 1).toCharArray()
            );
        }
        return null;
    }
}
