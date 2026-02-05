package sdmxdl.provider.ri.authenticators;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.ext.Cache;

import java.time.Duration;

public interface CredentialsCaching {

    @NonNull
    Cache<Credentials> getCredentialsCache(
            @NonNull Duration ttl,
            @Nullable EventListener onEvent,
            @Nullable ErrorListener onError);
}
