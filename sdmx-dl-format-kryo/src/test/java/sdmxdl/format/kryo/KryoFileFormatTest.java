package sdmxdl.format.kryo;

import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;
import org.junit.jupiter.api.Test;
import sdmxdl.DataRepository;
import sdmxdl.web.MonitorReports;
import tests.sdmxdl.api.RepoSamples;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;


public class KryoFileFormatTest {

    @Test
    public void testDataRepository() throws IOException {
        assertValid(KryoFileFormat.REPOSITORY, RepoSamples.EMPTY_REPO);

        DataRepository normal = RepoSamples.REPO
                .toBuilder()
                .ttl(Clock.systemDefaultZone().instant(), Duration.ofMillis(100))
                .build();
        assertValid(KryoFileFormat.REPOSITORY, normal);
    }

    @Test
    public void testMonitorReports() throws IOException {
        assertValid(KryoFileFormat.MONITOR, RepoSamples.EMPTY_REPORTS);

        MonitorReports normal = RepoSamples.REPORTS
                .toBuilder()
                .ttl(Clock.systemDefaultZone().instant(), Duration.ofMillis(100))
                .build();
        assertValid(KryoFileFormat.MONITOR, normal);
    }

    private static <T> void assertValid(KryoFileFormat<T> format, T data) throws IOException {
        assertThat(storeLoad(format, format, data))
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