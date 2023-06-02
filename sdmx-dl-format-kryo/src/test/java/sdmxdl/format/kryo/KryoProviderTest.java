package sdmxdl.format.kryo;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.format.FileFormatProviderAssert;


public class KryoProviderTest {

    @Test
    public void testCompliance() {
        FileFormatProviderAssert.assertCompliance(new KryoProvider());
    }
}