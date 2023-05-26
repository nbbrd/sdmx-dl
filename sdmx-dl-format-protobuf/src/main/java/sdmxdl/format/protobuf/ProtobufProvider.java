package sdmxdl.format.protobuf;

import lombok.NonNull;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataRepository;
import sdmxdl.format.FileFormat;
import sdmxdl.format.spi.FileFormatProvider;
import sdmxdl.web.MonitorReports;

import static nbbrd.io.FileFormatter.onFormattingStream;
import static nbbrd.io.FileParser.onParsingStream;
import static sdmxdl.format.protobuf.ProtobufMonitors.fromMonitorReports;
import static sdmxdl.format.protobuf.ProtobufMonitors.toMonitorReports;
import static sdmxdl.format.protobuf.ProtobufRepositories.fromDataRepository;
import static sdmxdl.format.protobuf.ProtobufRepositories.toDataRepository;

@ServiceProvider
public final class ProtobufProvider implements FileFormatProvider {

    @Override
    public @NonNull String getId() {
        return "PROTOBUF";
    }

    @Override
    public @NonNull FileFormat<MonitorReports> getMonitorReportsFormat() throws IllegalArgumentException {
        return new FileFormat<>(
                onParsingStream(resource -> toMonitorReports(sdmxdl.format.protobuf.web.MonitorReports.parseFrom(resource))),
                onFormattingStream((value, resource) -> fromMonitorReports(value).writeTo(resource)),
                ".protobuf"
        );
    }

    @Override
    public @NonNull FileFormat<DataRepository> getDataRepositoryFormat() throws IllegalArgumentException {
        return new FileFormat<>(
                onParsingStream(resource -> toDataRepository(sdmxdl.format.protobuf.DataRepository.parseFrom(resource))),
                onFormattingStream((value, resource) -> fromDataRepository(value).writeTo(resource)),
                ".protobuf");
    }
}
