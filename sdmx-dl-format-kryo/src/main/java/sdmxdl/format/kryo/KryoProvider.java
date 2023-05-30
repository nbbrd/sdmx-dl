package sdmxdl.format.kryo;

import lombok.NonNull;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataRepository;
import sdmxdl.format.FileFormat;
import sdmxdl.format.spi.FileFormatProvider;
import sdmxdl.web.MonitorReports;

@ServiceProvider
public final class KryoProvider implements FileFormatProvider {

    @Override
    public @NonNull String getId() {
        return "KRYO";
    }

    @Override
    public int getRank() {
        return 400;
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
