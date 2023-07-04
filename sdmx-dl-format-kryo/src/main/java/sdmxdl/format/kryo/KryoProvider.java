package sdmxdl.format.kryo;

import lombok.NonNull;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataRepository;
import sdmxdl.ext.spi.CacheProvider;
import sdmxdl.format.DiskCacheProviderSupport;
import sdmxdl.format.FileFormat;
import sdmxdl.format.spi.FileFormatProvider;
import sdmxdl.web.MonitorReports;

@ServiceProvider(FileFormatProvider.class)
@ServiceProvider(CacheProvider.class)
public final class KryoProvider implements FileFormatProvider, CacheProvider {

    private static final String ID = "KRYO";

    private static final int RANK = 400;

    @lombok.experimental.Delegate
    private final DiskCacheProviderSupport cacheProvider = DiskCacheProviderSupport
            .builder()
            .cacheId(ID)
            .cacheRank(RANK)
            .formatProvider(this)
            .build();

    @Override
    public @NonNull String getId() {
        return ID;
    }

    @Override
    public int getRank() {
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
