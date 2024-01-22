package sdmxdl.format.spi;

import lombok.NonNull;
import sdmxdl.DataRepository;
import sdmxdl.format.design.ServiceSupport;
import sdmxdl.format.WebSources;
import sdmxdl.web.MonitorReports;

@ServiceSupport
@lombok.Builder(toBuilder = true)
public final class PersistenceSupport implements Persistence {

    @lombok.NonNull
    private final String id;

    @lombok.Builder.Default
    private final int rank = UNKNOWN_PERSISTENCE_RANK;

    @lombok.NonNull
    @lombok.Builder.Default
    private final FileFormat<MonitorReports> monitor = FileFormat.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    private final FileFormat<DataRepository> repository = FileFormat.noOp();

    @lombok.NonNull
    @lombok.Builder.Default
    private final FileFormat<WebSources> sources = FileFormat.noOp();

    @Override
    public @NonNull String getPersistenceId() {
        return id;
    }

    @Override
    public int getPersistenceRank() {
        return rank;
    }

    @Override
    public @NonNull FileFormat<MonitorReports> getMonitorFormat() {
        return monitor;
    }

    @Override
    public @NonNull FileFormat<DataRepository> getRepositoryFormat() {
        return repository;
    }

    @Override
    public @NonNull FileFormat<WebSources> getSourcesFormat() {
        return sources;
    }
}
