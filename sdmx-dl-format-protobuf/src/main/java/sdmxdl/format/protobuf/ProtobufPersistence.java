package sdmxdl.format.protobuf;

import com.google.protobuf.MessageLite;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.ext.FileFormat;
import sdmxdl.ext.Persistence;
import sdmxdl.format.FileFormatSupport;
import sdmxdl.format.PersistenceSupport;
import sdmxdl.format.protobuf.web.MonitorReports;
import sdmxdl.format.protobuf.web.WebSources;

import java.io.IOException;
import java.io.OutputStream;

import static nbbrd.io.FileFormatter.onFormattingStream;
import static nbbrd.io.FileParser.onParsingStream;

@DirectImpl
@ServiceProvider
public final class ProtobufPersistence implements Persistence {

    @lombok.experimental.Delegate
    private final PersistenceSupport persistence = PersistenceSupport
            .builder()
            .id("PROTOBUF")
            .rank(300)
            .support(this::isSupported)
            .factory(this::create)
            .build();

    private boolean isSupported(Class<?> type) {
        return sdmxdl.DataRepository.class.equals(type)
                || sdmxdl.web.MonitorReports.class.equals(type)
                || sdmxdl.web.WebSources.class.equals(type);
    }

    @SuppressWarnings("unchecked")
    private <T> FileFormat<T> create(Class<T> type) {
        if (sdmxdl.DataRepository.class.equals(type)) {
            return (FileFormat<T>) FileFormatSupport
                    .builder(sdmxdl.DataRepository.class)
                    .parser(onParsingStream(DataRepository::parseFrom).andThen(ProtobufRepositories::toDataRepository))
                    .formatter(onFormattingStream(ProtobufPersistence::writeProtobuf).compose(ProtobufRepositories::fromDataRepository))
                    .extension(".protobuf")
                    .build();
        }
        if (sdmxdl.web.MonitorReports.class.equals(type)) {
            return (FileFormat<T>) FileFormatSupport
                    .builder(sdmxdl.web.MonitorReports.class)
                    .parser(onParsingStream(MonitorReports::parseFrom).andThen(ProtobufMonitors::toMonitorReports))
                    .formatter(onFormattingStream(ProtobufPersistence::writeProtobuf).compose(ProtobufMonitors::fromMonitorReports))
                    .extension(".protobuf")
                    .build();
        }
        if (sdmxdl.web.WebSources.class.equals(type)) {
            return (FileFormat<T>) FileFormatSupport
                    .builder(sdmxdl.web.WebSources.class)
                    .parser(onParsingStream(WebSources::parseFrom).andThen(ProtobufSources::toWebSources))
                    .formatter(onFormattingStream(ProtobufPersistence::writeProtobuf).compose(ProtobufSources::fromWebSources))
                    .extension(".protobuf")
                    .build();
        }
        return FileFormat.noOp();
    }

    private static void writeProtobuf(MessageLite message, OutputStream outputStream) throws IOException {
        message.writeTo(outputStream);
    }
}
