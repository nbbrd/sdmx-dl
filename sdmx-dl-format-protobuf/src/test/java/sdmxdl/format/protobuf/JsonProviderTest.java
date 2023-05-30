package sdmxdl.format.protobuf;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.format.FileFormatProviderAssert;

public class JsonProviderTest {

    @Test
    public void testCompliance() {
        FileFormatProviderAssert.assertCompliance(new JsonProvider());
    }
}
