package sdmxdl.format.kryo;

import lombok.NonNull;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataRepository;
import sdmxdl.ext.spi.CacheProvider;
import sdmxdl.format.DiskCacheProviderSupport;
import sdmxdl.format.FileFormat;
import sdmxdl.format.spi.Persistence;
import sdmxdl.web.MonitorReports;

@ServiceProvider(Persistence.class)
@ServiceProvider(CacheProvider.class)
public final class KryoProvider implements Persistence, CacheProvider {

    private static final String ID = "KRYO";

    private static final int RANK = 400;

    @lombok.experimental.Delegate
    private final DiskCacheProviderSupport caching = DiskCacheProviderSupport
            .builder()
            .cacheId(ID)
            .cacheRank(RANK)
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
