package internal.sdmxdl.format.kryo;

import lombok.NonNull;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataRepository;
import sdmxdl.format.FileFormat;
import sdmxdl.format.kryo.KryoFileFormat;
import sdmxdl.format.spi.FileFormatProvider;
import sdmxdl.web.MonitorReports;

@ServiceProvider
public final class KryoProvider implements FileFormatProvider {

    @Override
    public @NonNull String getName() {
        return "kryo";
    }

    @Override
    public @NonNull FileFormat<MonitorReports> getMonitorReportsFormat() throws IllegalArgumentException {
        return FileFormat.of(KryoFileFormat.MONITOR, ".kryo");
    }

    @Override
    public @NonNull FileFormat<DataRepository> getDataRepositoryFormat() throws IllegalArgumentException {
        return FileFormat.of(KryoFileFormat.REPOSITORY, ".kryo");
    }
}
