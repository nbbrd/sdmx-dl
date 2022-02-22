package sdmxdl.web;

import nl.altindag.log.LogCaptor;
import nl.altindag.log.model.LogEvent;
import org.junit.jupiter.api.Test;
import tests.sdmxdl.web.SdmxWebListenerAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

public class SdmxWebListenerTest {

    @Test
    public void testFactories() {
        assertThatNullPointerException().isThrownBy(() -> SdmxWebListener.of(null));
    }

    @Test
    public void testDefault() {
        LogCaptor logCaptor = LogCaptor.forName("internal.sdmxdl.LoggingListener");
        logCaptor.setLogLevelToInfo();

        SdmxWebListenerAssert.assertCompliance(SdmxWebListener.getDefault());

        assertThat(logCaptor.getLogEvents())
                .hasSize(1)
                .map(LogEvent::getMessage)
                .element(0, STRING)
                .isEqualTo("hello");
    }

    @Test
    public void testNoOp() {
        SdmxWebListenerAssert.assertCompliance(SdmxWebListener.noOp());
    }

    @Test
    public void testFunctional() {
        SdmxWebListenerAssert.assertCompliance(SdmxWebListener.of(this::doNothing));
    }

    private void doNothing(Object source, Object message) {
    }
}
