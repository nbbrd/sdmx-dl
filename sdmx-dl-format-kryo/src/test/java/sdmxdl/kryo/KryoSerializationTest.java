package sdmxdl.kryo;

import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;
import org.junit.jupiter.api.Test;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.samples.RepoSamples;
import sdmxdl.web.SdmxWebMonitorReport;
import sdmxdl.web.SdmxWebMonitorReports;
import sdmxdl.web.SdmxWebStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;


public class KryoSerializationTest {

    @Test
    public void test() throws IOException {
        Instant now = Clock.systemDefaultZone().instant();

        SdmxRepository repository = RepoSamples.REPO
                .toBuilder()
                .ttl(now, Duration.ofMillis(100))
                .build();

        assertThat(storeLoad(KryoFileFormat.REPOSITORY, KryoFileFormat.REPOSITORY, repository))
                .isEqualTo(repository)
                .isNotSameAs(repository);

        SdmxWebMonitorReports reports = SdmxWebMonitorReports
                .builder()
                .provider("abc")
                .report(SdmxWebMonitorReport.builder().source("xyz").status(SdmxWebStatus.DOWN).uptimeRatio(0.5).averageResponseTime(1234L).build())
                .ttl(now, Duration.ofMillis(100))
                .build();

        assertThat(storeLoad(KryoFileFormat.MONITOR, KryoFileFormat.MONITOR, reports))
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