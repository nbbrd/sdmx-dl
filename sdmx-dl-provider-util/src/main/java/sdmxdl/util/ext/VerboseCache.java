package sdmxdl.util.ext;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.ext.SdmxCache;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.web.SdmxWebMonitorReports;

import java.time.Clock;
import java.util.function.BiConsumer;

@lombok.AllArgsConstructor
public final class VerboseCache implements SdmxCache {

    @lombok.NonNull
    private final SdmxCache delegate;

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
    public SdmxRepository getRepository(@NonNull String key) {
        SdmxRepository result = delegate.getRepository(key);
        onRepository.accept(key, result != null);
        return result;
    }

    @Override
    public void putRepository(@NonNull String key, @NonNull SdmxRepository value) {
        delegate.putRepository(key, value);
    }

    @Override
    @Nullable
    public SdmxWebMonitorReports getWebMonitorReports(@NonNull String key) {
        SdmxWebMonitorReports result = delegate.getWebMonitorReports(key);
        onWebMonitorReports.accept(key, result != null);
        return result;
    }

    @Override
    public void putWebMonitorReports(@NonNull String key, @NonNull SdmxWebMonitorReports value) {
        delegate.putWebMonitorReports(key, value);
    }
}
