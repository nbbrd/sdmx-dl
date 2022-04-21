package tests.sdmxdl.ext;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public final class FakeClock extends Clock {

    private Instant current = Instant.now();

    public FakeClock set(Instant current) {
        this.current = current;
        return this;
    }

    public FakeClock set(long epochMilli) {
        return set(Instant.ofEpochMilli(epochMilli));
    }

    public FakeClock plus(long durationInMillis) {
        return set(current.plus(durationInMillis, ChronoUnit.MILLIS));
    }

    @Override
    public ZoneId getZone() {
        return ZoneId.systemDefault();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return current;
    }
}
