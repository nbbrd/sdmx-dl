package sdmxdl.ext;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.ext.CacheAssert;

public class CacheTest {

    @Test
    public void testNoOp() {
        CacheAssert.assertCompliance(Cache.noOp());
    }
}
