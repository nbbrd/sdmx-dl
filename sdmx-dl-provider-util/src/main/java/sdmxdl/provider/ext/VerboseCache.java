package sdmxdl.provider.ext;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataRepository;
import sdmxdl.ext.Cache;
import sdmxdl.web.MonitorReports;

import java.time.Clock;
import java.util.function.BiConsumer;

@lombok.AllArgsConstructor
public final class VerboseCache implements Cache {

    @lombok.NonNull
    private final Cache delegate;

    @lombok.NonNull
    private final BiConsumer<String, Boolean> onRepository;

    @lombok.NonNull
    private final BiConsumer<String, Boolean> onWebMonitorReports;

    @Override
    public @NonNull Clock getClock() {
        return delegate.getClock();
    }

    @Override
    @Nullable
    public DataRepository getRepository(@NonNull String key) {
        DataRepository result = delegate.getRepository(key);
        onRepository.accept(key, result != null);
        return result;
    }

    @Override
    public void putRepository(@NonNull String key, @NonNull DataRepository value) {
        delegate.putRepository(key, value);
    }

    @Override
    @Nullable
    public MonitorReports getMonitorReports(@NonNull String key) {
        MonitorReports result = delegate.getMonitorReports(key);
        onWebMonitorReports.accept(key, result != null);
        return result;
    }

    @Override
    public void putMonitorReports(@NonNull String key, @NonNull MonitorReports value) {
        delegate.putMonitorReports(key, value);
    }
}
