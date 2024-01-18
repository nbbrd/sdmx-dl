package sdmxdl.format.protobuf;

import com.google.protobuf.util.JsonFormat;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.format.spi.Persistence;
import sdmxdl.format.spi.PersistenceSupport;

import java.io.IOException;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nbbrd.io.text.TextFormatter.onFormattingWriter;
import static nbbrd.io.text.TextParser.onParsingReader;

@DirectImpl
@ServiceProvider
public final class JsonProvider implements Persistence {

    private final JsonFormat.Parser parser = JsonFormat.parser();

    private final JsonFormat.Printer formatter = JsonFormat.printer();

    @lombok.experimental.Delegate
    private final PersistenceSupport persistence = PersistenceSupport
            .builder()
            .id("JSON")
            .rank(200)
            .monitorReportsParser(onParsingReader(this::parseJsonReports).andThen(ProtobufMonitors::toMonitorReports).asFileParser(UTF_8))
            .monitorReportsFormatter(onFormattingWriter(formatter::appendTo).compose(ProtobufMonitors::fromMonitorReports).asFileFormatter(UTF_8))
            .dataRepositoryParser(onParsingReader(this::parseJsonRepository).andThen(ProtobufRepositories::toDataRepository).asFileParser(UTF_8))
            .dataRepositoryFormatter(onFormattingWriter(formatter::appendTo).compose(ProtobufRepositories::fromDataRepository).asFileFormatter(UTF_8))
            .webSourcesParser(onParsingReader(this::parseJsonSources).andThen(ProtobufSources::toWebSources).asFileParser(UTF_8))
            .webSourcesFormatter(onFormattingWriter(formatter::appendTo).compose(ProtobufSources::fromWebSources).asFileFormatter(UTF_8))
            .fileExtension(".json")
            .build();

    private sdmxdl.format.protobuf.web.MonitorReports parseJsonReports(Reader reader) throws IOException {
        sdmxdl.format.protobuf.web.MonitorReports.Builder builder = sdmxdl.format.protobuf.web.MonitorReports.newBuilder();
        parser.merge(reader, builder);
        return builder.build();
    }

    private sdmxdl.format.protobuf.DataRepository parseJsonRepository(Reader reader) throws IOException {
        sdmxdl.format.protobuf.DataRepository.Builder builder = sdmxdl.format.protobuf.DataRepository.newBuilder();
        parser.merge(reader, builder);
        return builder.build();
    }

    private sdmxdl.format.protobuf.web.WebSources parseJsonSources(Reader reader) throws IOException {
        sdmxdl.format.protobuf.web.WebSources.Builder builder = sdmxdl.format.protobuf.web.WebSources.newBuilder();
        parser.merge(reader, builder);
        return builder.build();
    }
}
