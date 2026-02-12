package sdmxdl.provider.ri.spi;

import internal.util.credentials.NoOpVaultService;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.ThreadSafe;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceFilter;
import nbbrd.service.ServiceId;
import org.jspecify.annotations.Nullable;
import sdmxdl.provider.Slow;

import java.io.IOException;

@ServiceDefinition(
        quantifier = Quantifier.SINGLE,
        fallback = NoOpVaultService.class,
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
    @Nullable String loadPassword(@NonNull String resource, @NonNull String userName) throws IOException;

    @Slow
    void storePassword(@NonNull String resource, @NonNull String userName, @Nullable String password) throws IOException;

    @StaticFactoryMethod
    static @NonNull VaultService noOp() {
        return NoOpVaultService.INSTANCE;
    }
}
