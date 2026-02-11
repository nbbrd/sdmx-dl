package sdmxdl.provider.ri.authenticators;

import com.microsoft.aad.msal4j.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;
import sdmxdl.provider.LockByKey;
import sdmxdl.format.design.PropertyDefinition;
import sdmxdl.provider.TypedId;
import sdmxdl.provider.ri.drivers.AuthSchemes;
import sdmxdl.provider.web.DriverProperties;
import sdmxdl.web.Credentials;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Authenticator;
import sdmxdl.web.spi.WebCaching;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Collections.emptyList;
import static nbbrd.io.http.HttpAuthenticator.newToken;
import static nbbrd.io.text.BaseProperty.keysOf;
import static sdmxdl.provider.web.DriverProperties.commaSeparatedProperty;

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

    private static final String ID = "MSAL";

    private final ExecutorService executor = Executors.newCachedThreadPool(SdmxWebManager::newLowPriorityDaemonThread);

    private final Duration ttl = Duration.ofMinutes(5);

    @Override
    public @NonNull String getAuthenticatorId() {
        return ID;
    }

    @Override
    public boolean isAuthenticatorAvailable() {
        return true;
    }

    @Override
    public @Nullable PasswordAuthentication getPasswordAuthenticationOrNull(@NonNull WebSource source,
                                                                            @NonNull WebCaching caching,
                                                                            @Nullable EventListener onEvent,
                                                                            @Nullable ErrorListener onError) throws IOException {
        MsalConfig config = MsalConfig.parse(source);
        if (config != null) {
            IPublicClientApplication app = PublicClientApplication
                    .builder(config.getClientId())
                    .authority(config.getAuthority())
                    .setTokenCacheAccessAspect(new CachedTokenCacheAccessAspect(
                            caching.getCredentialsCache(source, onEvent, onError), ttl, config.getUid()
                    ))
                    .executorService(executor)
                    .build();
            return newToken(acquireToken(app, config.getScopes(), config.getRedirectUri(), onEvent, config.getUid()).accessToken());
        }
        return null;
    }

    @Override
    public void invalidateAuthentication(@NonNull WebSource source,
                                         @NonNull WebCaching caching,
                                         @Nullable EventListener onEvent,
                                         @Nullable ErrorListener onError) throws IOException {
        MsalConfig config = MsalConfig.parse(source);
        if (config != null) {
            caching.getCredentialsCache(source, onEvent, onError).put(config.getUid(), null);
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
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
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

        @StaticFactoryMethod
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

        private static <T> @NonNull T getNotNull(@NonNull Property<T> property, @NonNull WebSource source) throws IOException {
            T value = property.get(source.getProperties());
            if (value == null) {
                throw new IOException("Property " + property.getKey() + " is not set for source " + source.getId());
            }
            return value;
        }
    }

    private static IAuthenticationResult acquireToken(IPublicClientApplication app, Set<String> scopes, URI redirectUri, EventListener onEvent, String uid) throws IOException {
        LockByKey lockByKey = new LockByKey();
        try {
            lockByKey.lock(uid);
            if (onEvent != null) onEvent.accept(ID, "Acquiring token silently for UID '" + uid + "'");
            return app.acquireTokenSilently(SilentParameters
                            .builder(scopes)
                            .account(app.getAccounts().join().stream().findFirst().orElse(null))
                            .build())
                    .join();
        } catch (CompletionException ex) {
            if (ex.getCause() instanceof MsalException) {
                if (onEvent != null) onEvent.accept(ID, "Acquiring token interactivity for UID '" + uid + "'");
                return app.acquireToken(InteractiveRequestParameters
                                .builder(redirectUri)
                                .scopes(scopes)
                                .prompt(Prompt.SELECT_ACCOUNT)
                                .build())
                        .join();
            } else {
                throw new IOException(ex.getCause());
            }
        } finally {
            lockByKey.unlock(uid);
        }
    }

    @lombok.AllArgsConstructor
    private static final class CachedTokenCacheAccessAspect implements ITokenCacheAccessAspect {

        private final @NonNull Cache<Credentials> cache;
        private final @NonNull Duration ttl;
        private final @NonNull String uid;

        @Override
        public void beforeCacheAccess(ITokenCacheAccessContext context) {
            Credentials token = cache.get(uid);
            if (token != null && !token.isEmpty())
                context.tokenCache().deserialize(String.valueOf(token.getCredentials().getPassword()));
        }

        @Override
        public void afterCacheAccess(ITokenCacheAccessContext context) {
            if (context.hasCacheChanged()) {
                cache.put(uid, Credentials.of(new PasswordAuthentication(uid, context.tokenCache().serialize().toCharArray()), cache.getClock().instant().plus(ttl)));
            }
        }
    }
}
