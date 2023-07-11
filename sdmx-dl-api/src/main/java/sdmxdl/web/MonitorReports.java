package sdmxdl.web;

import lombok.NonNull;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class MonitorReports {

    @NonNull String uriScheme;

    @lombok.Singular
    @NonNull List<MonitorReport> reports;

    @lombok.Builder.Default
    @NonNull Instant creationTime = Instant.EPOCH;

    @lombok.Builder.Default
    @NonNull Instant expirationTime = Instant.MAX;

    public boolean isExpired(@NonNull Clock clock) {
        return !clock.instant().isBefore(expirationTime);
    }

    public static final class Builder {

        public @NonNull Builder ttl(@NonNull Instant creationTime, @NonNull Duration ttl) {
            return creationTime(creationTime).expirationTime(creationTime.plus(ttl));
        }
    }
}
