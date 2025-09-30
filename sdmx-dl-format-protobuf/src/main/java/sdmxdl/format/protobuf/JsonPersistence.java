package sdmxdl.format.protobuf;

import com.google.protobuf.util.JsonFormat;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataRepository;
import sdmxdl.HasPersistence;
import sdmxdl.ext.FileFormat;
import sdmxdl.ext.Persistence;
import sdmxdl.format.FileFormatSupport;
import sdmxdl.format.PersistenceSupport;
import sdmxdl.format.protobuf.web.WebSourcesDto;
import sdmxdl.web.MonitorReports;
import sdmxdl.web.WebSources;

import java.io.IOException;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nbbrd.io.text.TextFormatter.onFormattingWriter;
import static nbbrd.io.text.TextParser.onParsingReader;

@DirectImpl
@ServiceProvider
public final class JsonPersistence implements Persistence {

    private final JsonFormat.Parser parser = JsonFormat.parser();

    private final JsonFormat.Printer formatter = JsonFormat.printer();

    @lombok.experimental.Delegate
    private final PersistenceSupport persistence = PersistenceSupport
            .builder()
            .id("JSON")
            .rank(200)
            .type(DataRepository.class)
            .type(MonitorReports.class)
            .type(WebSources.class)
            .factory(this::create)
            .build();

    @SuppressWarnings("unchecked")
    private <T extends HasPersistence> FileFormat<T> create(Class<T> type) {
        if (DataRepository.class.equals(type)) {
            return (FileFormat<T>) FileFormatSupport
                    .builder(DataRepository.class)
                    .parser(onParsingReader(this::parseJsonRepository).andThen(ProtoApi::toDataRepository).asFileParser(UTF_8))
                    .formatter(onFormattingWriter(formatter::appendTo).compose(ProtoApi::fromDataRepository).asFileFormatter(UTF_8))
                    .extension(".json")
                    .build();
        }
        if (MonitorReports.class.equals(type)) {
            return (FileFormat<T>) FileFormatSupport
                    .builder(MonitorReports.class)
                    .parser(onParsingReader(this::parseJsonReports).andThen(ProtoWeb::toMonitorReports).asFileParser(UTF_8))
                    .formatter(onFormattingWriter(formatter::appendTo).compose(ProtoWeb::fromMonitorReports).asFileFormatter(UTF_8))
                    .extension(".json")
                    .build();
        }
        if (WebSources.class.equals(type)) {
            return (FileFormat<T>) FileFormatSupport
                    .builder(WebSources.class)
                    .parser(onParsingReader(this::parseJsonSources).andThen(ProtoWeb::toWebSources).asFileParser(UTF_8))
                    .formatter(onFormattingWriter(formatter::appendTo).compose(ProtoWeb::fromWebSources).asFileFormatter(UTF_8))
                    .extension(".json")
                    .build();
        }
        return FileFormat.noOp();
    }

    private sdmxdl.format.protobuf.web.MonitorReportsDto parseJsonReports(Reader reader) throws IOException {
        sdmxdl.format.protobuf.web.MonitorReportsDto.Builder builder = sdmxdl.format.protobuf.web.MonitorReportsDto.newBuilder();
        parser.merge(reader, builder);
        return builder.build();
    }

    private DataRepositoryDto parseJsonRepository(Reader reader) throws IOException {
        DataRepositoryDto.Builder builder = DataRepositoryDto.newBuilder();
        parser.merge(reader, builder);
        return builder.build();
    }

    private WebSourcesDto parseJsonSources(Reader reader) throws IOException {
        WebSourcesDto.Builder builder = WebSourcesDto.newBuilder();
        parser.merge(reader, builder);
        return builder.build();
    }
}
