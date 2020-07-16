package sdmxdl.kryo;

import org.junit.Test;
import sdmxdl.samples.RepoSamples;
import sdmxdl.util.ext.ExpiringRepository;
import sdmxdl.util.ext.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;


public class KryoSerializationTest {

    @Test
    public void test() throws IOException {
        KryoSerialization x = new KryoSerialization();

        ExpiringRepository data = ExpiringRepository.of(Clock.systemDefaultZone(), Duration.ofMillis(100), RepoSamples.REPO);

        assertThat(storeLoad(x, data))
                .isEqualTo(data)
                .isNotSameAs(data);
    }

    private static ExpiringRepository storeLoad(Serializer x, ExpiringRepository data) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            x.store(output, data);
            try (ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray())) {
                return x.load(input);
            }
        }
    }
}