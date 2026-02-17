package sdmxdl.provider.caching;

import org.junit.jupiter.api.Test;
import sdmxdl.DataRepository;
import sdmxdl.ext.Cache;
import tests.sdmxdl.api.RepoSamples;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static tests.sdmxdl.ext.CacheAssert.assertRepositoryCompliance;

class LockingByKeyCacheTest {

    @Test
    void testCompliance() {
        assertRepositoryCompliance(new LockingByKeyCache<>(MemCache.<DataRepository>builder().build()));
    }

    @Test
    void testConcurrentAccess() {
        Cache<DataRepository> x = new LockingByKeyCache<>(MemCache.<DataRepository>builder().build());

        assertThatCode(() -> IntStream.range(0, 10).forEach(i -> x.put("key", RepoSamples.REPO)))
                .doesNotThrowAnyException();

        assertThat(x.get("key")).isEqualTo(RepoSamples.REPO);
    }
}