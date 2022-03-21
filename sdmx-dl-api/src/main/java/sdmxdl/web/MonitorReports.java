package sdmxdl.web;

import lombok.NonNull;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class MonitorReports {

    @lombok.NonNull
    String uriScheme;

    @lombok.Singular
    List<MonitorReport> reports;

    @lombok.NonNull
    @lombok.Builder.Default
    Instant creationTime = Instant.EPOCH;

    @lombok.NonNull
    @lombok.Builder.Default
    Instant expirationTime = Instant.MAX;

    public boolean isExpired(@NonNull Clock clock) {
        return !clock.instant().isBefore(expirationTime);
    }

    public static final class Builder {

        public Builder ttl(Instant creationTime, Duration ttl) {
            return creationTime(creationTime).expirationTime(creationTime.plus(ttl));
        }
    }
}
