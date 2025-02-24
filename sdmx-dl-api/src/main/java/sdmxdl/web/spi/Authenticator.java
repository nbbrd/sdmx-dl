package sdmxdl.web.spi;

import lombok.NonNull;
import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceFilter;
import nbbrd.service.ServiceId;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.web.WebSource;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.Collection;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        loaderName = "internal.{{canonicalName}}Loader"
)
@ThreadSafe
public interface Authenticator {

    @ServiceId(pattern = ServiceId.SCREAMING_SNAKE_CASE)
    @NonNull String getAuthenticatorId();

    @ServiceFilter
    boolean isAuthenticatorAvailable();

    @Nullable PasswordAuthentication getPasswordAuthenticationOrNull(@NonNull WebSource source) throws IOException;

    void invalidateAuthentication(@NonNull WebSource source) throws IOException;

    @NonNull Collection<String> getAuthenticatorProperties();

    String AUTHENTICATOR_PROPERTY_PREFIX = "sdmxdl.authenticator";
}
