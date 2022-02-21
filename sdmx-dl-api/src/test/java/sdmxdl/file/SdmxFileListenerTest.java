package sdmxdl.file;

import nl.altindag.log.LogCaptor;
import nl.altindag.log.model.LogEvent;
import org.junit.jupiter.api.Test;
import tests.sdmxdl.file.SdmxFileListenerAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

public class SdmxFileListenerTest {

    @Test
    public void testDefault() {
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
    public void testNoOp() {
        SdmxFileListenerAssert.assertCompliance(SdmxFileListener.noOp());
    }

    @Test
    public void testFunctional() {
        SdmxFileListenerAssert.assertCompliance(SdmxFileListener.of(this::doNothing));
    }

    private void doNothing(Object source, Object message) {
    }
}
