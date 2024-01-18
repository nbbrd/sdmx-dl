package sdmxdl.format.spi;

import internal.sdmxdl.format.NoOpPersistence;
import lombok.NonNull;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import nbbrd.service.ServiceSorter;
import sdmxdl.DataRepository;
import sdmxdl.format.FileFormat;
import sdmxdl.format.WebSources;
import sdmxdl.web.MonitorReports;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE
)
public interface Persistence {

    @ServiceId
    @NonNull String getPersistenceId();

    @ServiceSorter(reverse = true)
    int getPersistenceRank();

    @NonNull FileFormat<MonitorReports> getMonitorReportsFormat() throws IllegalArgumentException;

    @NonNull FileFormat<DataRepository> getDataRepositoryFormat() throws IllegalArgumentException;

    @NonNull FileFormat<WebSources> getWebSourcesFormat() throws IllegalArgumentException;

    int UNKNOWN_PERSISTENCE_RANK = -1;

    static @NonNull Persistence noOp() {
        return NoOpPersistence.INSTANCE;
    }
}
