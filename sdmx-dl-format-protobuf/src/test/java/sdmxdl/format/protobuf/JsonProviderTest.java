package sdmxdl.format.protobuf;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.format.FileFormattingAssert;

public class JsonProviderTest {

    @Test
    public void testCompliance() {
        FileFormattingAssert.assertCompliance(new JsonProvider());
    }
}
