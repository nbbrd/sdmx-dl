package sdmxdl.web;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class SdmxWebListenerTest {

    @Test
    public void testFactories() {
        assertThatNullPointerException().isThrownBy(() -> SdmxWebListener.of(null));
    }
}
