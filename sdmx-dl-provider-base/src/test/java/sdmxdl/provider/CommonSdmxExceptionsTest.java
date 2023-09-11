package sdmxdl.provider;

import org.junit.jupiter.api.Test;
import sdmxdl.CodelistRef;
import sdmxdl.StructureRef;
import sdmxdl.FlowRef;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.provider.CommonSdmxExceptions.*;

@SuppressWarnings({"ConstantConditions", "ThrowableNotThrown"})
public class CommonSdmxExceptionsTest {

    private static final Marker SOURCE = Marker.parse("abc");
    private static final FlowRef DATAFLOW_REF = FlowRef.parse("df");
    private static final StructureRef DATA_STRUCTURE_REF = StructureRef.parse("ds");
    private static final CodelistRef CODELIST_REF = CodelistRef.parse("cl");

    @Test
    public void testConnectionClosed() {
        assertThatNullPointerException()
                .isThrownBy(() -> connectionClosed(null));

        assertThat(connectionClosed(() -> SOURCE))
                .hasNoCause()
                .hasMessageContainingAll(SOURCE);
    }

    @Test
    public void testMissingFlow() {
        assertThatNullPointerException()
                .isThrownBy(() -> missingFlow(null, DATAFLOW_REF));

        assertThatNullPointerException()
                .isThrownBy(() -> missingFlow(() -> SOURCE, null));

        assertThat(missingFlow(() -> SOURCE, DATAFLOW_REF))
                .hasNoCause()
                .hasMessageContainingAll(SOURCE)
                .hasMessageContaining(DATAFLOW_REF.getId());
    }

    @Test
    public void testMissingStructure() {
        assertThatNullPointerException()
                .isThrownBy(() -> missingStructure(null, DATA_STRUCTURE_REF));

        assertThatNullPointerException()
                .isThrownBy(() -> missingStructure(() -> SOURCE, null));

        assertThat(missingStructure(() -> SOURCE, DATA_STRUCTURE_REF))
                .hasNoCause()
                .hasMessageContainingAll(SOURCE)
                .hasMessageContaining(DATA_STRUCTURE_REF.getId());
    }

    @Test
    public void testMissingCodelist() {
        assertThatNullPointerException()
                .isThrownBy(() -> missingCodelist(null, CODELIST_REF));

        assertThatNullPointerException()
                .isThrownBy(() -> missingCodelist(() -> SOURCE, null));

        assertThat(missingCodelist(() -> SOURCE, CODELIST_REF))
                .hasNoCause()
                .hasMessageContainingAll(SOURCE)
                .hasMessageContaining(CODELIST_REF.getId());
    }
}
