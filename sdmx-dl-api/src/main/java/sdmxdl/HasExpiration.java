package sdmxdl;

import lombok.NonNull;

import java.time.Clock;
import java.time.Instant;

/**
 * Defines the ability to expire.
 */
public interface HasExpiration {

    @NonNull Instant getExpirationTime();

    default boolean isExpired(@NonNull Clock clock) {
        return !clock.instant().isBefore(getExpirationTime());
    }
}
