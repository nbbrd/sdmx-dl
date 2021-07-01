package internal.sdmxdl.ri.web;

import org.junit.Test;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class SdmxResourceTypeTest {

    @Test
    public void testApplyOn() {
        for (SdmxResourceType x : SdmxResourceType.values()) {
            assertThatNullPointerException()
                    .isThrownBy(() -> x.applyOn(null));

            assertThat(x.applyOn(singletonMap(x, "hello")))
                    .contains("hello");

            assertThat(x.applyOn(singletonMap(x, null)))
                    .isEmpty();

            assertThat(x.applyOn(emptyMap()))
                    .isEmpty();
        }
    }
}
