package sdmxdl.format.kryo;

import lombok.NonNull;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataRepository;
import sdmxdl.file.spi.FileCaching;
import sdmxdl.format.DiskCachingSupport;
import sdmxdl.format.FileFormat;
import sdmxdl.format.spi.Persistence;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.spi.WebCaching;

@ServiceProvider(Persistence.class)
@ServiceProvider(FileCaching.class)
@ServiceProvider(WebCaching.class)
public final class KryoProvider implements Persistence, FileCaching, WebCaching {

    private static final String ID = "KRYO";

    private static final int RANK = 400;

    @lombok.experimental.Delegate
    private final DiskCachingSupport caching = DiskCachingSupport
            .builder()
            .id(ID)
            .rank(RANK)
            .persistence(this)
            .build();

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
}
