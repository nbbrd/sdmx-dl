package sdmxdl.util.parser;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.ext.ObsFactoryAssert;

public class DialectsTest {

    @Test
    public void testCompliance() {
        ObsFactoryAssert.assertCompliance(DefaultObsParser::newDefault);
    }
}
