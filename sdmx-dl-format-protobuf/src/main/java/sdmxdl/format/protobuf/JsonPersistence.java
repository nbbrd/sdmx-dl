package sdmxdl.format.protobuf;

import com.google.protobuf.util.JsonFormat;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.HasPersistence;
import sdmxdl.ext.FileFormat;
import sdmxdl.ext.Persistence;
import sdmxdl.format.FileFormatSupport;
import sdmxdl.format.PersistenceSupport;

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
                    .parser(onParsingReader(this::parseJsonRepository).andThen(ProtoApi::toDataRepository).asFileParser(UTF_8))
                    .formatter(onFormattingWriter(formatter::appendTo).compose(ProtoApi::fromDataRepository).asFileFormatter(UTF_8))
                    .extension(".json")
                    .build();
        }
        if (sdmxdl.web.MonitorReports.class.equals(type)) {
            return (FileFormat<T>) FileFormatSupport
                    .builder(sdmxdl.web.MonitorReports.class)
                    .parser(onParsingReader(this::parseJsonReports).andThen(ProtoWeb::toMonitorReports).asFileParser(UTF_8))
                    .formatter(onFormattingWriter(formatter::appendTo).compose(ProtoWeb::fromMonitorReports).asFileFormatter(UTF_8))
                    .extension(".json")
                    .build();
        }
        if (sdmxdl.web.WebSources.class.equals(type)) {
            return (FileFormat<T>) FileFormatSupport
                    .builder(sdmxdl.web.WebSources.class)
                    .parser(onParsingReader(this::parseJsonSources).andThen(ProtoWeb::toWebSources).asFileParser(UTF_8))
                    .formatter(onFormattingWriter(formatter::appendTo).compose(ProtoWeb::fromWebSources).asFileFormatter(UTF_8))
                    .extension(".json")
                    .build();
        }
        return FileFormat.noOp();
    }

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
