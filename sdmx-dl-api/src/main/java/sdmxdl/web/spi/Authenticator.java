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

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        loaderName = "internal.util.AuthenticatorLoader"
)
@ThreadSafe
public interface Authenticator {

    @ServiceId
    @NonNull String getAuthenticatorId();

    @ServiceFilter
    boolean isAuthenticatorAvailable();

    @Nullable PasswordAuthentication getPasswordAuthenticationOrNull(@NonNull WebSource source) throws IOException;

    void invalidateAuthentication(@NonNull WebSource source) throws IOException;
}
