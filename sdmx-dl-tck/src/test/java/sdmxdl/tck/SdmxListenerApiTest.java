package sdmxdl.tck;

import org.junit.Test;
import sdmxdl.file.SdmxFileListener;
import sdmxdl.tck.file.SdmxFileListenerAssert;
import sdmxdl.tck.web.SdmxWebListenerAssert;
import sdmxdl.web.SdmxWebListener;

public class SdmxListenerApiTest {

    @Test
    public void testDefaultWebListener() {
        SdmxWebListenerAssert.assertCompliance(SdmxWebListener.getDefault());
    }

    @Test
    public void testNoOpWebListener() {
        SdmxWebListenerAssert.assertCompliance(SdmxWebListener.noOp());
    }

    @Test
    public void testFunctionalWebListener() {
        SdmxWebListenerAssert.assertCompliance(SdmxWebListener.of(this::doNothing));
    }

    @Test
    public void testDefaultFileListener() {
        SdmxFileListenerAssert.assertCompliance(SdmxFileListener.getDefault());
    }

    @Test
    public void testNoOpFileListener() {
        SdmxFileListenerAssert.assertCompliance(SdmxFileListener.noOp());
    }

    @Test
    public void testFunctionalFileListener() {
        SdmxFileListenerAssert.assertCompliance(SdmxFileListener.of(this::doNothing));
    }

    private void doNothing(Object source, Object message) {
    }
}
