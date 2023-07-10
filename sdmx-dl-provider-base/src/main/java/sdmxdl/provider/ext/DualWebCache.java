package sdmxdl.provider.ext;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.WebCache;

import java.time.Clock;

@lombok.AllArgsConstructor
public final class DualWebCache implements WebCache {

    private final @NonNull WebCache first;
    private final @NonNull WebCache second;
    private final @NonNull Clock clock;

    @Override
    public @NonNull Clock getWebClock() {
        return clock;
    }

    @Override
    public @Nullable DataRepository getWebRepository(@NonNull String key) {
        DataRepository result = first.getWebRepository(key);
        if (result == null) {
            result = second.getWebRepository(key);
            if (result != null) {
                first.putWebRepository(key, result);
            }
        }
        return result;
    }

    @Override
    public void putWebRepository(@NonNull String key, @NonNull DataRepository value) {
        first.putWebRepository(key, value);
        second.putWebRepository(key, value);
    }

    @Override
    public @Nullable MonitorReports getWebMonitorReports(@NonNull String key) {
        MonitorReports result = first.getWebMonitorReports(key);
        if (result == null) {
            result = second.getWebMonitorReports(key);
            if (result != null) {
                first.putWebMonitorReports(key, result);
            }
        }
        return result;
    }

    @Override
    public void putWebMonitorReports(@NonNull String key, @NonNull MonitorReports value) {
        first.putWebMonitorReports(key, value);
        second.putWebMonitorReports(key, value);
    }
}
