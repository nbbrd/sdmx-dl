package sdmxdl.ext;

import org.junit.jupiter.api.Test;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;

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
                .isThrownBy(() -> SdmxException.missingData(null, DataflowRef.parse("xyz")));
    }
}
