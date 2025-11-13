package sdmxdl.provider.ri.authenticators;

import com.microsoft.aad.msal4j.*;
import internal.util.credentials.WinPasswordVault;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.sys.OS;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.provider.TypedId;
import sdmxdl.provider.ri.drivers.AuthSchemes;
import sdmxdl.provider.web.DriverProperties;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Authenticator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;

import static java.util.Collections.emptyList;
import static nbbrd.io.function.IOFunction.unchecked;
import static nbbrd.io.http.HttpAuthenticator.newToken;
import static nbbrd.io.text.BaseProperty.keysOf;
import static sdmxdl.provider.web.DriverProperties.commaSeparatedProperty;

@lombok.extern.java.Log
@DirectImpl
@ServiceProvider
public final class MsalAuthenticator implements Authenticator {

    @PropertyDefinition
    public static final Property<String> UID_PROPERTY =
            Property.of(AUTHENTICATOR_PROPERTY_PREFIX + ".uid", null, Parser.onString(), Formatter.onString());

    @PropertyDefinition
    public static final Property<String> CLIENT_ID_PROPERTY =
            Property.of(AUTHENTICATOR_PROPERTY_PREFIX + ".clientId", null, Parser.onString(), Formatter.onString());

    @PropertyDefinition
    public static final Property<String> AUTHORITY_PROPERTY =
            Property.of(AUTHENTICATOR_PROPERTY_PREFIX + ".authority", null, Parser.onString(), Formatter.onString());

    @PropertyDefinition
    public static final Property<List<String>> SCOPES_PROPERTY =
            commaSeparatedProperty(AUTHENTICATOR_PROPERTY_PREFIX + ".scopes", emptyList());

    @PropertyDefinition
    public static final Property<URI> REDIRECT_URI_PROPERTY =
            Property.of(AUTHENTICATOR_PROPERTY_PREFIX + ".redirectUri", URI.create("http://localhost"), Parser.onURI(), Formatter.onURI());

    private final ConcurrentMap<String, IPublicClientApplication> cache = new ConcurrentHashMap<>();

    @Override
    public @NonNull String getAuthenticatorId() {
        return "MSAL";
    }

    @Override
    public boolean isAuthenticatorAvailable() {
        return true;
    }

    @Override
    public @Nullable PasswordAuthentication getPasswordAuthenticationOrNull(@NonNull WebSource source) throws IOException {
        MsalConfig config = MsalConfig.parse(source);
        if (config != null) {
            IPublicClientApplication app = getClientApplication(config);
            return newToken(acquireToken(app, config.getScopes(), config.getRedirectUri()).accessToken());
        }
        return null;
    }

    @Override
    public void invalidateAuthentication(@NonNull WebSource source) throws IOException {
        MsalConfig config = MsalConfig.parse(source);
        if (config != null) {
            cache.remove(config.getUid());
        }
    }

    @Override
    public @NonNull Collection<String> getAuthenticatorProperties() {
        return keysOf(
                UID_PROPERTY,
                CLIENT_ID_PROPERTY,
                AUTHORITY_PROPERTY,
                SCOPES_PROPERTY,
                REDIRECT_URI_PROPERTY
        );
    }

    @VisibleForTesting
    @lombok.Value
    static class MsalConfig {

        @NonNull
        String uid;

        @NonNull
        String clientId;

        @NonNull
        String authority;

        @NonNull
        Set<String> scopes;

        @NonNull
        URI redirectUri;

        public static @Nullable MsalConfig parse(@NonNull WebSource source) throws IOException {
            if (AuthSchemes.MSAL_AUTH_SCHEME.equals(DriverProperties.AUTH_SCHEME_PROPERTY.get(source.getProperties()))) {
                String uid = UID_PROPERTY.get(source.getProperties());
                return new MsalConfig(
                        uid != null && !uid.isEmpty() ? uid : TypedId.getUniqueID(source),
                        getNotNull(CLIENT_ID_PROPERTY, source),
                        getNotNull(AUTHORITY_PROPERTY, source),
                        new HashSet<>(getNotNull(SCOPES_PROPERTY, source)),
                        getNotNull(REDIRECT_URI_PROPERTY, source)
                );
            }
            return null;
        }
    }

    private IPublicClientApplication getClientApplication(MsalConfig config) throws IOException {
        try {
            return cache.computeIfAbsent(config.getUid(), unchecked(uid -> newClientApplication(config)));
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    private static IPublicClientApplication newClientApplication(MsalConfig config) throws MalformedURLException {
        return PublicClientApplication
                .builder(config.getClientId())
                .authority(config.getAuthority())
                .setTokenCacheAccessAspect(newTokenPersistence(config.getUid(), config.getUid()))
                .build();
    }

    private static ITokenCacheAccessAspect newTokenPersistence(String resource, String userName) {
        return OS.NAME.equals(OS.Name.WINDOWS)
                ? new VaultTokenPersistence(resource, userName, (msg, e) -> log.log(Level.SEVERE, msg, e))
                : NoOpTokenPersistence.INSTANCE;
    }

    private static IAuthenticationResult acquireToken(IPublicClientApplication app, Set<String> scopes, URI redirectUri) throws IOException {
        synchronized (app) {
            try {
                return app.acquireTokenSilently(SilentParameters
                                .builder(scopes)
                                .account(app.getAccounts().join().stream().findFirst().orElse(null))
                                .build())
                        .join();
            } catch (CompletionException ex) {
                if (ex.getCause() instanceof MsalException) {
                    return app.acquireToken(InteractiveRequestParameters
                                    .builder(redirectUri)
                                    .scopes(scopes)
                                    .prompt(Prompt.SELECT_ACCOUNT)
                                    .build())
                            .join();
                } else {
                    throw new IOException(ex.getCause());
                }
            }
        }
    }

    private static <T> @NonNull T getNotNull(@NonNull Property<T> property, @NonNull WebSource source) throws IOException {
        T value = property.get(source.getProperties());
        if (value == null) {
            throw new IOException("Property " + property.getKey() + " is not set for source " + source.getId());
        }
        return value;
    }

    private enum NoOpTokenPersistence implements ITokenCacheAccessAspect {

        INSTANCE;

        @Override
        public void beforeCacheAccess(ITokenCacheAccessContext context) {
            // No-op
        }

        @Override
        public void afterCacheAccess(ITokenCacheAccessContext context) {
            // No-op
        }
    }

    @lombok.AllArgsConstructor
    private static final class VaultTokenPersistence implements ITokenCacheAccessAspect {

        private final String resource;
        private final String userName;
        private final BiConsumer<? super String, ? super IOException> onError;

        @Override
        public void beforeCacheAccess(ITokenCacheAccessContext context) {
            try (WinPasswordVault vault = WinPasswordVault.open()) {
                WinPasswordVault.PasswordCredential credential = vault.get(resource);
                if (credential != null && credential.getUserName().equals(userName)) {
                    context.tokenCache().deserialize(String.valueOf(credential.getPassword()));
                }
            } catch (IOException e) {
                onError.accept("Failed to access token cache from Windows Password Vault", e);
            }
        }

        @Override
        public void afterCacheAccess(ITokenCacheAccessContext context) {
            if (context.hasCacheChanged()) {
                try (WinPasswordVault vault = WinPasswordVault.open()) {
                    vault.invalidate(resource);
                    vault.add(new WinPasswordVault.PasswordCredential(resource, userName, context.tokenCache().serialize().toCharArray()));
                } catch (IOException e) {
                    onError.accept("Failed to update token cache in Windows Password Vault", e);
                }
            }
        }
    }
}
