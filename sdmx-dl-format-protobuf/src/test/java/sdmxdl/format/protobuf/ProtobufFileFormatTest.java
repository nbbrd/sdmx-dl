package sdmxdl.format.protobuf;

import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;
import org.junit.jupiter.api.Test;
import sdmxdl.DataRepository;
import tests.sdmxdl.api.RepoSamples;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class ProtobufFileFormatTest {

    @Test
    public void test() throws IOException {
        Instant now = Clock.systemDefaultZone().instant();

        DataRepository repository = RepoSamples.REPO
                .toBuilder()
                .ttl(now, Duration.ofMillis(100))
                .build();

        assertThat(storeLoad(ProtobufRepositories.getFileParser(), ProtobufRepositories.getFileFormatter(), repository))
                .isEqualTo(repository)
                .isNotSameAs(repository);

        sdmxdl.web.MonitorReports reports = RepoSamples.REPORTS
                .toBuilder()
                .ttl(now, Duration.ofMillis(100))
                .build();

        assertThat(storeLoad(ProtobufMonitors.getFileParser(), ProtobufMonitors.getFileFormatter(), reports))
                .isEqualTo(reports)
                .isNotSameAs(reports);
    }

    private static <T> T storeLoad(FileParser<T> parser, FileFormatter<T> formatter, T data) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            formatter.formatStream(data, output);
            try (ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray())) {
                return parser.parseStream(input);
            }
        }
    }
}
