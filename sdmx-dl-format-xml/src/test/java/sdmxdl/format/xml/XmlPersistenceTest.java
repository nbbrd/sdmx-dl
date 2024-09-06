package sdmxdl.format.xml;

import org.junit.jupiter.api.Test;
import tests.sdmxdl.ext.PersistenceAssert;


public class XmlPersistenceTest {

    @Test
    public void testCompliance() {
        PersistenceAssert.assertCompliance(new XmlPersistence());
    }
}