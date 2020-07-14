package _test.sdmxdl.util;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public final class FakeClock extends Clock {

    private Instant current = Instant.now();

    public void set(Instant current) {
        this.current = current;
    }

    public void set(long epochMilli) {
        this.current = Instant.ofEpochMilli(epochMilli);
    }

    public void plus(long durationInMillis) {
        current = current.plus(durationInMillis, ChronoUnit.MILLIS);
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
