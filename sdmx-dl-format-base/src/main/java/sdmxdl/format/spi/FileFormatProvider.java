package sdmxdl.format.spi;

import lombok.NonNull;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import nbbrd.service.ServiceSorter;
import sdmxdl.DataRepository;
import sdmxdl.format.FileFormat;
import sdmxdl.web.MonitorReports;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE
)
public interface FileFormatProvider {

    @ServiceId
    @NonNull String getId();

    @ServiceSorter(reverse = true)
    int getRank();

    @NonNull FileFormat<MonitorReports> getMonitorReportsFormat() throws IllegalArgumentException;

    @NonNull FileFormat<DataRepository> getDataRepositoryFormat() throws IllegalArgumentException;
}
