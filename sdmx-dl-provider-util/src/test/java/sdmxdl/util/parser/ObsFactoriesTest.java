package sdmxdl.util.parser;

import org.junit.jupiter.api.Test;
import sdmxdl.tck.ext.ObsFactoryAssert;

public class ObsFactoriesTest {

    @Test
    public void testCompliance() {
        ObsFactoryAssert.assertCompliance(ObsFactories.SDMX20);
        ObsFactoryAssert.assertCompliance(ObsFactories.SDMX21);
    }
}
