package sdmxdl.format.protobuf;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.format.PersistenceAssert;

public class JsonPersistenceTest {

    @Test
    public void testCompliance() {
        PersistenceAssert.assertCompliance(new JsonPersistence());
    }
}
