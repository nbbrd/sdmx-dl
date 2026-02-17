package internal.sdmxdl;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.api.RepoSamples;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static tests.sdmxdl.api.RepoSamples.REPO;
import static tests.sdmxdl.ext.CacheAssert.assertCompliance;

class NoOpCacheTest {

    @Test
    void testCompliance() {
        assertCompliance(NoOpCache.INSTANCE, (creationTime, ttl) -> REPO.toBuilder().ttl(creationTime, ttl).build());
    }

    @Test
    void testNoOp() {
        NoOpCache x = NoOpCache.INSTANCE;

        assertThatCode(() -> IntStream.range(0, 10).forEach(i -> x.put("key", RepoSamples.REPO)))
                .doesNotThrowAnyException();

        assertThat(x.get("key")).isNull();
    }
}