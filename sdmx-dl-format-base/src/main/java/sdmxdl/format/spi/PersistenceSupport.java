package sdmxdl.format.spi;

import lombok.NonNull;
import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;
import sdmxdl.DataRepository;
import sdmxdl.format.FileFormat;
import sdmxdl.format.WebSources;
import sdmxdl.web.MonitorReports;

@lombok.Builder(toBuilder = true)
public final class PersistenceSupport implements Persistence {

    @lombok.NonNull
    private final String id;

    @lombok.Builder.Default
    private final int rank = UNKNOWN_PERSISTENCE_RANK;

    @lombok.NonNull
    private final FileParser<MonitorReports> monitorReportsParser;

    @lombok.NonNull
    private final FileFormatter<MonitorReports> monitorReportsFormatter;

    @lombok.NonNull
    private final FileParser<DataRepository> dataRepositoryParser;

    @lombok.NonNull
    private final FileFormatter<DataRepository> dataRepositoryFormatter;

    @lombok.NonNull
    private final FileParser<WebSources> webSourcesParser;

    @lombok.NonNull
    private final FileFormatter<WebSources> webSourcesFormatter;

    @lombok.NonNull
    private final String fileExtension;

    @Override
    public @NonNull String getPersistenceId() {
        return id;
    }

    @Override
    public int getPersistenceRank() {
        return rank;
    }

    @Override
    public @NonNull FileFormat<MonitorReports> getMonitorReportsFormat() throws IllegalArgumentException {
        return FileFormat.of(monitorReportsParser, monitorReportsFormatter, fileExtension);
    }

    @Override
    public @NonNull FileFormat<DataRepository> getDataRepositoryFormat() throws IllegalArgumentException {
        return FileFormat.of(dataRepositoryParser, dataRepositoryFormatter, fileExtension);
    }

    @Override
    public @NonNull FileFormat<WebSources> getWebSourcesFormat() throws IllegalArgumentException {
        return FileFormat.of(webSourcesParser, webSourcesFormatter, fileExtension);
    }
}
