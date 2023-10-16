package sdmxdl.format.time;

import org.junit.jupiter.api.Test;
import sdmxdl.format.DiskCachingSupport;

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
