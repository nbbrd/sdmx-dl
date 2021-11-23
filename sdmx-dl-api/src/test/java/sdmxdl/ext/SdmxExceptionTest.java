package sdmxdl.ext;

import org.junit.jupiter.api.Test;
import sdmxdl.*;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@SuppressWarnings("ConstantConditions")
public class SdmxExceptionTest {

    @Test
    public void testFactories() {
        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.connectionClosed(null));

        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.missingFlow(null, DataflowRef.parse("xyz")));

        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.missingFlow("abc", null));

        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.missingStructure(null, DataStructureRef.parse("xyz")));

        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.missingStructure("abc", null));

        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.missingData(null, DataRef.of(DataflowRef.parse("xyz"), Key.ALL, DataFilter.FULL)));

        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.invalidKey(null, Key.ALL, "cause"));

        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.invalidKey("abc", null, "cause"));

        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.invalidKey("abc", Key.ALL, null));
    }
}
