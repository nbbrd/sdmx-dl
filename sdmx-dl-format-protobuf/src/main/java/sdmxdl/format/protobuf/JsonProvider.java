package sdmxdl.format.protobuf;

import com.google.protobuf.util.JsonFormat;
import lombok.NonNull;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataRepository;
import sdmxdl.ext.spi.CacheProvider;
import sdmxdl.format.DiskCacheProviderSupport;
import sdmxdl.format.FileFormat;
import sdmxdl.format.spi.FileFormatProvider;
import sdmxdl.web.MonitorReports;

import java.io.IOException;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nbbrd.io.text.TextFormatter.onFormattingWriter;
import static nbbrd.io.text.TextParser.onParsingReader;

@ServiceProvider(FileFormatProvider.class)
@ServiceProvider(CacheProvider.class)
public final class JsonProvider implements FileFormatProvider, CacheProvider {

    private static final String ID = "JSON";

    private static final int RANK = 200;

    private final JsonFormat.Parser jsonParser = JsonFormat.parser();

    private final JsonFormat.Printer jsonFormatter = JsonFormat.printer();

    @lombok.experimental.Delegate
    private final DiskCacheProviderSupport cacheProvider = DiskCacheProviderSupport
            .builder()
            .cacheId(ID)
            .cacheRank(RANK)
            .formatProvider(this)
            .build();

    @Override
    public @NonNull String getId() {
        return ID;
    }

    @Override
    public int getRank() {
        return RANK;
    }

    @Override
    public @NonNull FileFormat<MonitorReports> getMonitorReportsFormat() throws IllegalArgumentException {
        return new FileFormat<>(
                onParsingReader(this::parseJsonReports).andThen(ProtobufMonitors::toMonitorReports).asFileParser(UTF_8),
                onFormattingWriter(jsonFormatter::appendTo).compose(ProtobufMonitors::fromMonitorReports).asFileFormatter(UTF_8),
                ".json"
        );
    }

    @Override
    public @NonNull FileFormat<DataRepository> getDataRepositoryFormat() throws IllegalArgumentException {
        return new FileFormat<>(
                onParsingReader(this::parseJsonRepository).andThen(ProtobufRepositories::toDataRepository).asFileParser(UTF_8),
                onFormattingWriter(jsonFormatter::appendTo).compose(ProtobufRepositories::fromDataRepository).asFileFormatter(UTF_8),
                ".json");
    }

    private sdmxdl.format.protobuf.web.MonitorReports parseJsonReports(Reader reader) throws IOException {
        sdmxdl.format.protobuf.web.MonitorReports.Builder builder = sdmxdl.format.protobuf.web.MonitorReports.newBuilder();
        jsonParser.merge(reader, builder);
        return builder.build();
    }

    private sdmxdl.format.protobuf.DataRepository parseJsonRepository(Reader reader) throws IOException {
        sdmxdl.format.protobuf.DataRepository.Builder builder = sdmxdl.format.protobuf.DataRepository.newBuilder();
        jsonParser.merge(reader, builder);
        return builder.build();
    }
}
