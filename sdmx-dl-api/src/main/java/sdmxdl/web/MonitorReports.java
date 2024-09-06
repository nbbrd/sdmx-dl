package sdmxdl.web;

import lombok.NonNull;
import sdmxdl.HasExpiration;
import sdmxdl.HasPersistence;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class MonitorReports implements HasPersistence, HasExpiration {

    @NonNull String uriScheme;

    @lombok.Singular
    @NonNull List<MonitorReport> reports;

    @lombok.Builder.Default
    @NonNull Instant creationTime = Instant.EPOCH;

    @lombok.Builder.Default
    @NonNull Instant expirationTime = Instant.MAX;

    public static final class Builder {

        public @NonNull Builder ttl(@NonNull Instant creationTime, @NonNull Duration ttl) {
            return creationTime(creationTime).expirationTime(creationTime.plus(ttl));
        }
    }
}
