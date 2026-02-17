package sdmxdl.provider.caching;

import org.junit.jupiter.api.Test;

import static tests.sdmxdl.file.spi.FileCachingAssert.assertFileCompliance;
import static tests.sdmxdl.web.spi.WebCachingAssert.assertWebCompliance;

public class DiskCachingSupportTest {

    @Test
    public void testCompliance() {
        DiskCachingSupport x = DiskCachingSupport.builder().id("COMPLIANCE").build();
        assertFileCompliance(x);
        assertWebCompliance(x);
    }
}
