package sdmxdl.util.ext;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.repo.SdmxRepository;

import java.time.Clock;
import java.time.Duration;

@lombok.Value(staticConstructor = "of")
public class ExpiringRepository {

    @NonNull
    public static ExpiringRepository of(@NonNull Clock clock, @NonNull Duration ttl, @NonNull SdmxRepository value) {
        return ExpiringRepository.of(clock.millis() + ttl.toMillis(), value);
    }

    private long expirationTimeInMillis;

    @lombok.NonNull
    private SdmxRepository value;

    public boolean isExpired(@NonNull Clock clock) {
        return expirationTimeInMillis <= clock.millis();
    }
}
