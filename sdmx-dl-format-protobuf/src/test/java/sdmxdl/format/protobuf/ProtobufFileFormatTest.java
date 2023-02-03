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

import static org.assertj.core.api.Assertions.assertThat;

public class ProtobufFileFormatTest {

    @Test
    public void testDataRepository() throws IOException {
        assertValid(ProtobufRepositories.getFileParser(), ProtobufRepositories.getFileFormatter(), RepoSamples.EMPTY_REPO);

        DataRepository nullFlowDescription = RepoSamples.EMPTY_REPO
                .toBuilder()
                .flow(RepoSamples.FLOW.toBuilder().description(null).build())
                .build();
        assertValid(ProtobufRepositories.getFileParser(), ProtobufRepositories.getFileFormatter(), nullFlowDescription);

        DataRepository nullTimeDimensionId = RepoSamples.EMPTY_REPO
                .toBuilder()
                .structure(RepoSamples.STRUCT.toBuilder().timeDimensionId(null).build())
                .build();
        assertValid(ProtobufRepositories.getFileParser(), ProtobufRepositories.getFileFormatter(), nullTimeDimensionId);

        DataRepository normal = RepoSamples.REPO
                .toBuilder()
                .ttl(Clock.systemDefaultZone().instant(), Duration.ofMillis(100))
                .build();
        assertValid(ProtobufRepositories.getFileParser(), ProtobufRepositories.getFileFormatter(), normal);
    }

    @Test
    public void testMonitorReports() throws IOException {
        assertValid(ProtobufMonitors.getFileParser(), ProtobufMonitors.getFileFormatter(), RepoSamples.EMPTY_REPORTS);

        sdmxdl.web.MonitorReports normal = RepoSamples.REPORTS
                .toBuilder()
                .ttl(Clock.systemDefaultZone().instant(), Duration.ofMillis(100))
                .build();
        assertValid(ProtobufMonitors.getFileParser(), ProtobufMonitors.getFileFormatter(), normal);
    }

    private static <T> void assertValid(FileParser<T> parser, FileFormatter<T> formatter, T data) throws IOException {
        assertThat(storeLoad(parser, formatter, data))
                .isEqualTo(data)
                .isNotSameAs(data);
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
