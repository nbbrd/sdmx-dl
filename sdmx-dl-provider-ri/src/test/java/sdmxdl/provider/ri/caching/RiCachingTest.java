package sdmxdl.provider.ri.caching;

import org.junit.jupiter.api.Test;

import static tests.sdmxdl.file.spi.FileCachingAssert.assertFileCompliance;
import static tests.sdmxdl.web.spi.WebCachingAssert.assertWebCompliance;

public class RiCachingTest {

    @Test
    public void testCompliance() {
        RiCaching x = new RiCaching();
        assertFileCompliance(x);
        assertWebCompliance(x);
    }
}
