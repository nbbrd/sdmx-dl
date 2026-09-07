package sdmxdl.provider.caching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static tests.sdmxdl.ext.CacheAssert.assertRepositoryCompliance;

import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import sdmxdl.DataRepository;
import sdmxdl.ext.Cache;
import sdmxdl.provider.KeyedLock;
import tests.sdmxdl.api.RepoSamples;

class LockingByKeyCacheTest {

    @Test
    void testCompliance() {
        assertRepositoryCompliance(
                new LockingByKeyCache<>(MemCache.<DataRepository>builder().build(), KeyedLock.newInstance()));
    }

    @Test
    void testConcurrentAccess() {
        Cache<DataRepository> x =
                new LockingByKeyCache<>(MemCache.<DataRepository>builder().build(), KeyedLock.newInstance());

        assertThatCode(() -> IntStream.range(0, 10).forEach(i -> x.put("key", RepoSamples.REPO)))
                .doesNotThrowAnyException();

        assertThat(x.get("key")).isEqualTo(RepoSamples.REPO);
    }
}
