package sdmxdl.kryo;

import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;
import org.junit.Test;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.samples.RepoSamples;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;


public class KryoSerializationTest {

    @Test
    public void test() throws IOException {
        Instant now = Clock.systemDefaultZone().instant();

        SdmxRepository data = RepoSamples.REPO
                .toBuilder()
                .creationTime(now)
                .expirationTime(now.plus(100, ChronoUnit.MILLIS))
                .build();

        assertThat(storeLoad(KryoSerialization.getRepositoryParser(), KryoSerialization.getRepositoryFormatter(), data))
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