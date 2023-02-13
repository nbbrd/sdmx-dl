package sdmxdl.format.spi;

import lombok.NonNull;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import sdmxdl.DataRepository;
import sdmxdl.format.FileFormat;
import sdmxdl.web.MonitorReports;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE
)
public interface FileFormatProvider {

    //    @ServiceId
    @NonNull String getId();

    @NonNull FileFormat<MonitorReports> getMonitorReportsFormat() throws IllegalArgumentException;

    @NonNull FileFormat<DataRepository> getDataRepositoryFormat() throws IllegalArgumentException;
}
