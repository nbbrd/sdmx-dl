package sdmxdl.format.protobuf;

import com.google.protobuf.MessageLite;
import lombok.NonNull;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataRepository;
import sdmxdl.format.FileFormat;
import sdmxdl.format.spi.FileFormatProvider;
import sdmxdl.web.MonitorReports;

import java.io.IOException;
import java.io.OutputStream;

import static nbbrd.io.FileFormatter.onFormattingStream;
import static nbbrd.io.FileParser.onParsingStream;

@ServiceProvider
public final class ProtobufProvider implements FileFormatProvider {

    @Override
    public @NonNull String getId() {
        return "PROTOBUF";
    }

    @Override
    public int getRank() {
        return 300;
    }

    @Override
    public @NonNull FileFormat<MonitorReports> getMonitorReportsFormat() throws IllegalArgumentException {
        return new FileFormat<>(
                onParsingStream(sdmxdl.format.protobuf.web.MonitorReports::parseFrom).andThen(ProtobufMonitors::toMonitorReports),
                onFormattingStream(this::writeProtobuf).compose(ProtobufMonitors::fromMonitorReports),
                ".protobuf"
        );
    }

    @Override
    public @NonNull FileFormat<DataRepository> getDataRepositoryFormat() throws IllegalArgumentException {
        return new FileFormat<>(
                onParsingStream(sdmxdl.format.protobuf.DataRepository::parseFrom).andThen(ProtobufRepositories::toDataRepository),
                onFormattingStream(this::writeProtobuf).compose(ProtobufRepositories::fromDataRepository),
                ".protobuf");
    }

    private void writeProtobuf(MessageLite message, OutputStream outputStream) throws IOException {
        message.writeTo(outputStream);
    }
}
