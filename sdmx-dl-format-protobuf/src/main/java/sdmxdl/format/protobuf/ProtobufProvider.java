package sdmxdl.format.protobuf;

import com.google.protobuf.MessageLite;
import nbbrd.service.ServiceProvider;
import sdmxdl.ext.spi.Caching;
import sdmxdl.format.DiskCachingSupport;
import sdmxdl.format.protobuf.web.MonitorReports;
import sdmxdl.format.spi.Persistence;
import sdmxdl.format.spi.PersistenceSupport;

import java.io.IOException;
import java.io.OutputStream;

import static nbbrd.io.FileFormatter.onFormattingStream;
import static nbbrd.io.FileParser.onParsingStream;

@ServiceProvider(Persistence.class)
@ServiceProvider(Caching.class)
public final class ProtobufProvider implements Persistence, Caching {

    @lombok.experimental.Delegate
    private final PersistenceSupport persistence = PersistenceSupport
            .builder()
            .id("PROTOBUF")
            .rank(300)
            .monitorReportsParser(onParsingStream(MonitorReports::parseFrom).andThen(ProtobufMonitors::toMonitorReports))
            .monitorReportsFormatter(onFormattingStream(this::writeProtobuf).compose(ProtobufMonitors::fromMonitorReports))
            .dataRepositoryParser(onParsingStream(DataRepository::parseFrom).andThen(ProtobufRepositories::toDataRepository))
            .dataRepositoryFormatter(onFormattingStream(this::writeProtobuf).compose(ProtobufRepositories::fromDataRepository))
            .fileExtension(".protobuf")
            .build();

    @lombok.experimental.Delegate
    private final DiskCachingSupport caching = DiskCachingSupport
            .builder()
            .id(persistence.getPersistenceId())
            .rank(persistence.getPersistenceRank())
            .persistence(persistence)
            .build();

    private void writeProtobuf(MessageLite message, OutputStream outputStream) throws IOException {
        message.writeTo(outputStream);
    }
}
