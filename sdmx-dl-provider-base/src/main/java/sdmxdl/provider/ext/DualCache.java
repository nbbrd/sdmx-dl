package sdmxdl.provider.ext;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ext.Cache;
import sdmxdl.web.MonitorReports;

import java.time.Clock;

@lombok.AllArgsConstructor
public final class DualCache implements Cache {

    private final @NonNull Cache first;
    private final @NonNull Cache second;
    private final @NonNull Clock clock;

    @Override
    public @NonNull Clock getClock() {
        return clock;
    }

    @Override
    public @Nullable DataRepository getRepository(@NonNull String key) {
        DataRepository result = first.getRepository(key);
        if (result == null) {
            result = second.getRepository(key);
            if (result != null) {
                first.putRepository(key, result);
            }
        }
        return result;
    }

    @Override
    public void putRepository(@NonNull String key, @NonNull DataRepository value) {
        first.putRepository(key, value);
        second.putRepository(key, value);
    }

    @Override
    public @Nullable MonitorReports getMonitorReports(@NonNull String key) {
        MonitorReports result = first.getMonitorReports(key);
        if (result == null) {
            result = second.getMonitorReports(key);
            if (result != null) {
                first.putMonitorReports(key, result);
            }
        }
        return result;
    }

    @Override
    public void putMonitorReports(@NonNull String key, @NonNull MonitorReports value) {
        first.putMonitorReports(key, value);
        second.putMonitorReports(key, value);
    }
}
