package sdmxdl.format.kryo;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.format.FileFormattingAssert;


public class KryoProviderTest {

    @Test
    public void testCompliance() {
        FileFormattingAssert.assertCompliance(new KryoProvider());
    }
}