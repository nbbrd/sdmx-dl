package sdmxdl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.CodelistRef.of;
import static sdmxdl.ResourceRef.ALL_AGENCIES;
import static sdmxdl.ResourceRef.LATEST_VERSION;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("DataFlowIssue")
public class CodelistRefTest {

    @Test
    public void testParse() {
        assertThat(CodelistRef.parse("hello")).isEqualTo(of(null, "hello", null));
        assertThat(CodelistRef.parse("world,hello")).isEqualTo(of("world", "hello", null));
        assertThat(CodelistRef.parse("world,hello,123")).isEqualTo(of("world", "hello", "123"));
        assertThatIllegalArgumentException().isThrownBy(() -> CodelistRef.parse("a,b,c,d"));
        assertThatNullPointerException().isThrownBy(() -> CodelistRef.parse(null));
    }

    @Test
    public void testOf() {
        assertThat(of(null, "CL_FREQ", null))
                .returns(ALL_AGENCIES, CodelistRef::getAgency)
                .returns("CL_FREQ", CodelistRef::getId)
                .returns(LATEST_VERSION, CodelistRef::getVersion)
                .hasToString("all,CL_FREQ,latest")
                .returns("CL_FREQ", CodelistRef::toShortString);

        assertThat(of("world", "CL_FREQ", "123"))
                .returns("world", CodelistRef::getAgency)
                .returns("CL_FREQ", CodelistRef::getId)
                .returns("123", CodelistRef::getVersion)
                .hasToString("world,CL_FREQ,123")
                .returns("world,CL_FREQ,123", CodelistRef::toShortString);

        assertThatIllegalArgumentException().isThrownBy(() -> of(null, "a,b", null));
        assertThatNullPointerException().isThrownBy(() -> of(null, null, null));
    }

    @Test
    public void testEquals() {
        assertThat(of("world", "CL_FREQ", "123"))
                .isEqualTo(of("world", "CL_FREQ", "123"))
                .isNotEqualTo(of("world", "CL_OTHER", "123"));
    }
}

