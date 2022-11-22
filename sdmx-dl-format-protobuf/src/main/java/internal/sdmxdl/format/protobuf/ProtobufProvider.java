package internal.sdmxdl.format.protobuf;

import lombok.NonNull;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataRepository;
import sdmxdl.format.FileFormat;
import sdmxdl.format.protobuf.ProtobufMonitors;
import sdmxdl.format.protobuf.ProtobufRepositories;
import sdmxdl.format.spi.FileFormatProvider;
import sdmxdl.web.MonitorReports;

@ServiceProvider
public final class ProtobufProvider implements FileFormatProvider {

    @Override
    public @NonNull String getName() {
        return "protobuf";
    }

    @Override
    public @NonNull FileFormat<MonitorReports> getMonitorReportsFormat() throws IllegalArgumentException {
        return new FileFormat<>(ProtobufMonitors.getFileParser(), ProtobufMonitors.getFileFormatter(), ".protobuf");
    }

    @Override
    public @NonNull FileFormat<DataRepository> getDataRepositoryFormat() throws IllegalArgumentException {
        return new FileFormat<>(ProtobufRepositories.getFileParser(), ProtobufRepositories.getFileFormatter(), ".protobuf");
    }
}
