package sdmxdl.ext;

import org.junit.jupiter.api.Test;
import sdmxdl.web.spi.WebCache;
import tests.sdmxdl.ext.CacheAssert;

public class WebCacheTest {

    @Test
    public void testNoOp() {
        CacheAssert.assertCompliance(WebCache.noOp());
    }
}
