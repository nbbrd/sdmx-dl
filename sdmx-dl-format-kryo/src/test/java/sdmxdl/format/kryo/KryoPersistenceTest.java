package sdmxdl.format.kryo;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.ext.PersistenceAssert;


public class KryoPersistenceTest {

    @Test
    public void testCompliance() {
        PersistenceAssert.assertCompliance(new KryoPersistence());
    }
}