package sdmxdl.format.protobuf;

import com.google.protobuf.MessageLite;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.format.protobuf.web.MonitorReports;
import sdmxdl.format.protobuf.web.WebSources;
import sdmxdl.format.spi.FileFormatSupport;
import sdmxdl.format.spi.Persistence;
import sdmxdl.format.spi.PersistenceSupport;

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
            .monitor(FileFormatSupport
                    .builder(sdmxdl.web.MonitorReports.class)
                    .parsing(true)
                    .parser(onParsingStream(MonitorReports::parseFrom).andThen(ProtobufMonitors::toMonitorReports))
                    .formatting(true)
                    .formatter(onFormattingStream(ProtobufPersistence::writeProtobuf).compose(ProtobufMonitors::fromMonitorReports))
                    .extension(".protobuf")
                    .build())
            .repository(FileFormatSupport
                    .builder(sdmxdl.DataRepository.class)
                    .parsing(true)
                    .parser(onParsingStream(DataRepository::parseFrom).andThen(ProtobufRepositories::toDataRepository))
                    .formatting(true)
                    .formatter(onFormattingStream(ProtobufPersistence::writeProtobuf).compose(ProtobufRepositories::fromDataRepository))
                    .extension(".protobuf")
                    .build())
            .sources(FileFormatSupport
                    .builder(sdmxdl.format.WebSources.class)
                    .parsing(true)
                    .parser(onParsingStream(WebSources::parseFrom).andThen(ProtobufSources::toWebSources))
                    .formatting(true)
                    .formatter(onFormattingStream(ProtobufPersistence::writeProtobuf).compose(ProtobufSources::fromWebSources))
                    .extension(".protobuf")
                    .build())
            .build();

    private static void writeProtobuf(MessageLite message, OutputStream outputStream) throws IOException {
        message.writeTo(outputStream);
    }
}
