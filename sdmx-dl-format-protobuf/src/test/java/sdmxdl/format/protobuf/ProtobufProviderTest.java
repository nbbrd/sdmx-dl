package sdmxdl.format.protobuf;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.format.FileFormatProviderAssert;

public class ProtobufProviderTest {

    @Test
    public void testCompliance() {
        FileFormatProviderAssert.assertCompliance(new ProtobufProvider());
    }
}
