package internal.sdmxdl;

import org.junit.jupiter.api.Test;

import static tests.sdmxdl.file.spi.FileCachingAssert.assertFileCompliance;
import static tests.sdmxdl.web.spi.WebCachingAssert.assertWebCompliance;

public class NoOpCachingTest {

    @Test
    public void testCompliance() {
        NoOpCaching x = NoOpCaching.INSTANCE;
        assertFileCompliance(x);
        assertWebCompliance(x);
    }
}
