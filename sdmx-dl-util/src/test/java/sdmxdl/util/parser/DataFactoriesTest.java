package sdmxdl.util.parser;

import org.junit.Test;
import sdmxdl.tck.ObsFactoryAssert;

public class DataFactoriesTest {

    @Test
    public void testCompliance() {
        ObsFactoryAssert.assertCompliance(DataFactories.SDMX20);
        ObsFactoryAssert.assertCompliance(DataFactories.SDMX21);
    }
}
