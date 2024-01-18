package sdmxdl.format.kryo;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataRepository;
import sdmxdl.format.FileFormat;
import sdmxdl.format.WebSources;
import sdmxdl.format.spi.Persistence;
import sdmxdl.web.MonitorReports;

@DirectImpl
@ServiceProvider
public final class KryoProvider implements Persistence {

    private static final String ID = "KRYO";

    private static final int RANK = 400;

    @Override
    public @NonNull String getPersistenceId() {
        return ID;
    }

    @Override
    public int getPersistenceRank() {
        return RANK;
    }

    @Override
    public @NonNull FileFormat<MonitorReports> getMonitorReportsFormat() throws IllegalArgumentException {
        return FileFormat.of(new KryoFileFormat<>(MonitorReports.class), ".kryo");
    }

    @Override
    public @NonNull FileFormat<DataRepository> getDataRepositoryFormat() throws IllegalArgumentException {
        return FileFormat.of(new KryoFileFormat<>(DataRepository.class), ".kryo");
    }

    @Override
    public @NonNull FileFormat<WebSources> getWebSourcesFormat() throws IllegalArgumentException {
        return FileFormat.of(new KryoFileFormat<>(WebSources.class), ".kryo");
    }
}
