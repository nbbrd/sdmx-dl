package sdmxdl.ext;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.ext.SdmxCacheAssert;

public class SdmxCacheTest {

    @Test
    public void testNoOp() {
        SdmxCacheAssert.assertCompliance(SdmxCache.noOp());
    }
}
