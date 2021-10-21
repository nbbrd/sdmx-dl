package sdmxdl.tck;

import nl.altindag.log.LogCaptor;
import nl.altindag.log.model.LogEvent;
import org.junit.jupiter.api.Test;
import sdmxdl.file.SdmxFileListener;
import sdmxdl.tck.file.SdmxFileListenerAssert;
import sdmxdl.tck.web.SdmxWebListenerAssert;
import sdmxdl.web.SdmxWebListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

public class SdmxListenerApiTest {

    @Test
    public void testDefaultWebListener() {
        LogCaptor logCaptor = LogCaptor.forName("internal.sdmxdl.SdmxListeners");
        logCaptor.setLogLevelToInfo();

        SdmxWebListenerAssert.assertCompliance(SdmxWebListener.getDefault());

        assertThat(logCaptor.getLogEvents())
                .hasSize(1)
                .map(LogEvent::getMessage)
                .element(0, STRING)
                .isEqualTo("hello");
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
        LogCaptor logCaptor = LogCaptor.forName("internal.sdmxdl.SdmxListeners");
        logCaptor.setLogLevelToInfo();

        SdmxFileListenerAssert.assertCompliance(SdmxFileListener.getDefault());

        assertThat(logCaptor.getLogEvents())
                .hasSize(1)
                .map(LogEvent::getMessage)
                .element(0, STRING)
                .isEqualTo("hello");
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
