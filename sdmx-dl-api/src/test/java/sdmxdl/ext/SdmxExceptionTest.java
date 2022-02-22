package sdmxdl.ext;

import org.junit.jupiter.api.Test;
import sdmxdl.CodelistRef;
import sdmxdl.DataStructureRef;
import sdmxdl.DataflowRef;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

@SuppressWarnings({"ConstantConditions", "ThrowableNotThrown"})
public class SdmxExceptionTest {

    @Test
    public void testConnectionClosed() {
        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.connectionClosed(null));

        assertThat(SdmxException.connectionClosed("abc"))
                .hasNoCause()
                .extracting(SdmxException::getSource, STRING)
                .isEqualTo("abc");
    }

    @Test
    public void testMissingFlow() {
        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.missingFlow(null, DataflowRef.parse("xyz")));

        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.missingFlow("abc", null));

        assertThat(SdmxException.missingFlow("abc", DataflowRef.parse("xyz")))
                .hasNoCause()
                .extracting(SdmxException::getSource, STRING)
                .isEqualTo("abc");
    }

    @Test
    public void testMissingStructure() {
        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.missingStructure(null, DataStructureRef.parse("xyz")));

        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.missingStructure("abc", null));

        assertThat(SdmxException.missingStructure("abc", DataStructureRef.parse("xyz")))
                .hasNoCause()
                .extracting(SdmxException::getSource, STRING)
                .isEqualTo("abc");
    }

    @Test
    public void testMissingData() {
        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.missingData(null, DataflowRef.parse("xyz")));

        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.missingData("abc", null));

        assertThat(SdmxException.missingData("abc", DataflowRef.parse("xyz")))
                .hasNoCause()
                .extracting(SdmxException::getSource, STRING)
                .isEqualTo("abc");
    }

    @Test
    public void testMissingCodelist() {
        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.missingCodelist(null, CodelistRef.parse("xyz")));

        assertThatNullPointerException()
                .isThrownBy(() -> SdmxException.missingCodelist("abc", null));

        assertThat(SdmxException.missingCodelist("abc", CodelistRef.parse("xyz")))
                .hasNoCause()
                .extracting(SdmxException::getSource, STRING)
                .isEqualTo("abc");
    }
}
