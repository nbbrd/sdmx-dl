package sdmxdl.format.protobuf;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.ext.PersistenceAssert;

public class ProtobufPersistenceTest {

    @Test
    public void testCompliance() {
        PersistenceAssert.assertCompliance(new ProtobufPersistence());
    }
}
