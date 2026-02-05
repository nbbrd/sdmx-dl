package sdmxdl.provider.ri.spi;

import lombok.NonNull;
import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceFilter;
import nbbrd.service.ServiceId;
import org.jspecify.annotations.Nullable;
import sdmxdl.provider.Slow;

import java.io.IOException;
import java.net.PasswordAuthentication;

@ServiceDefinition(
        quantifier = Quantifier.OPTIONAL,
        loaderName = "internal.{{canonicalName}}Loader"
)
@ThreadSafe
public interface VaultService {

    @ServiceId(pattern = ServiceId.SCREAMING_SNAKE_CASE)
    @NonNull
    String getVaultId();

    @ServiceFilter
    boolean isVaultAvailable();

    @Slow
    @Nullable PasswordAuthentication loadCredentials(@NonNull String id) throws IOException;

    @Slow
    void storeCredentials(@NonNull String id, @Nullable PasswordAuthentication credentials) throws IOException;
}
