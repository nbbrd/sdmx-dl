package sdmxdl.tck;

import org.junit.Test;
import sdmxdl.ext.SdmxCache;
import sdmxdl.tck.ext.SdmxCacheAssert;

public class SdmxCacheApiTest {

    @Test
    public void testNoOpCache() {
        SdmxCacheAssert.assertCompliance(SdmxCache.noOp());
    }
}
