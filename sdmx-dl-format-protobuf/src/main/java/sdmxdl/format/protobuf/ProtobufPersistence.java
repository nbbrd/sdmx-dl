package sdmxdl.format.protobuf;

import com.google.protobuf.MessageLite;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.HasPersistence;
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
            .type(sdmxdl.DataRepository.class)
            .type(sdmxdl.web.MonitorReports.class)
            .type(sdmxdl.web.WebSources.class)
            .factory(this::create)
            .build();

    @SuppressWarnings("unchecked")
    private <T extends HasPersistence> FileFormat<T> create(Class<T> type) {
        if (sdmxdl.DataRepository.class.equals(type)) {
            return (FileFormat<T>) FileFormatSupport
                    .builder(sdmxdl.DataRepository.class)
                    .parser(onParsingStream(DataRepository::parseFrom).andThen(ProtoApi::toDataRepository))
                    .formatter(onFormattingStream(ProtobufPersistence::writeProtobuf).compose(ProtoApi::fromDataRepository))
                    .extension(".protobuf")
                    .build();
        }
        if (sdmxdl.web.MonitorReports.class.equals(type)) {
            return (FileFormat<T>) FileFormatSupport
                    .builder(sdmxdl.web.MonitorReports.class)
                    .parser(onParsingStream(MonitorReports::parseFrom).andThen(ProtoWeb::toMonitorReports))
                    .formatter(onFormattingStream(ProtobufPersistence::writeProtobuf).compose(ProtoWeb::fromMonitorReports))
                    .extension(".protobuf")
                    .build();
        }
        if (sdmxdl.web.WebSources.class.equals(type)) {
            return (FileFormat<T>) FileFormatSupport
                    .builder(sdmxdl.web.WebSources.class)
                    .parser(onParsingStream(WebSources::parseFrom).andThen(ProtoWeb::toWebSources))
                    .formatter(onFormattingStream(ProtobufPersistence::writeProtobuf).compose(ProtoWeb::fromWebSources))
                    .extension(".protobuf")
                    .build();
        }
        return FileFormat.noOp();
    }

    private static void writeProtobuf(MessageLite message, OutputStream outputStream) throws IOException {
        message.writeTo(outputStream);
    }
}
