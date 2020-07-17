package sdmxdl.util.parser;

import org.junit.Test;
import sdmxdl.tck.ObsFactoryAssert;

public class ObsFactoriesTest {

    @Test
    public void testCompliance() {
        ObsFactoryAssert.assertCompliance(ObsFactories.SDMX20);
        ObsFactoryAssert.assertCompliance(ObsFactories.SDMX21);
    }
}
