package sdmxdl.ext;

import org.junit.jupiter.api.Test;

import static tests.sdmxdl.ext.CacheAssert.assertMonitorCompliance;
import static tests.sdmxdl.ext.CacheAssert.assertRepositoryCompliance;

public class CacheTest {

    @Test
    public void testNoOp() {
        assertMonitorCompliance(Cache.noOp());
        assertRepositoryCompliance(Cache.noOp());
    }
}
