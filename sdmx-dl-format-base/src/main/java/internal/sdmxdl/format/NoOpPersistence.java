package internal.sdmxdl.format;

import lombok.NonNull;
import sdmxdl.DataRepository;
import sdmxdl.format.WebSources;
import sdmxdl.format.spi.FileFormat;
import sdmxdl.format.spi.Persistence;
import sdmxdl.web.MonitorReports;

public enum NoOpPersistence implements Persistence {

    INSTANCE;

    @Override
    public @NonNull String getPersistenceId() {
        return "NO_OP";
    }

    @Override
    public int getPersistenceRank() {
        return UNKNOWN_PERSISTENCE_RANK;
    }

    @Override
    public @NonNull FileFormat<MonitorReports> getMonitorFormat() {
        return FileFormat.noOp();
    }

    @Override
    public @NonNull FileFormat<DataRepository> getRepositoryFormat() {
        return FileFormat.noOp();
    }

    @Override
    public @NonNull FileFormat<WebSources> getSourcesFormat() {
        return FileFormat.noOp();
    }
}
