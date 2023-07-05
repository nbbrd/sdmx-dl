package sdmxdl.format.spi;

import lombok.NonNull;
import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;
import sdmxdl.DataRepository;
import sdmxdl.format.FileFormat;
import sdmxdl.web.MonitorReports;

@lombok.Builder(toBuilder = true)
public final class PersistenceSupport implements Persistence {

    @lombok.Getter
    @lombok.NonNull
    private final String persistenceId;

    @lombok.Getter
    @lombok.Builder.Default
    private final int persistenceRank = UNKNOWN_PERSISTENCE_RANK;

    @lombok.NonNull
    private final FileParser<MonitorReports> monitorReportsParser;

    @lombok.NonNull
    private final FileFormatter<MonitorReports> monitorReportsFormatter;

    @lombok.NonNull
    private final FileParser<DataRepository> dataRepositoryParser;

    @lombok.NonNull
    private final FileFormatter<DataRepository> dataRepositoryFormatter;

    @lombok.NonNull
    private final String fileExtension;

    @Override
    public @NonNull FileFormat<MonitorReports> getMonitorReportsFormat() throws IllegalArgumentException {
        return new FileFormat<>(monitorReportsParser, monitorReportsFormatter, fileExtension);
    }

    @Override
    public @NonNull FileFormat<DataRepository> getDataRepositoryFormat() throws IllegalArgumentException {
        return new FileFormat<>(dataRepositoryParser, dataRepositoryFormatter, fileExtension);
    }
}
