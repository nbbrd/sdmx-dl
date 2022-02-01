package sdmxdl.util.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.util.SdmxPatterns.FLOW_REF_PATTERN;

@SuppressWarnings("ConstantConditions")
public class ValidatorTest {

    @Test
    public void testOnRegex() {
        assertThatNullPointerException()
                .isThrownBy(() -> Validator.onRegex("DataflowRef", null));

        assertThatNullPointerException()
                .isThrownBy(() -> Validator.onRegex(null, FLOW_REF_PATTERN));

        Validator<String> x = Validator.onRegex("DataflowRef", FLOW_REF_PATTERN);

        assertThat((String) null).satisfies(value -> {
            String msg = "Expecting DataflowRef 'null' to match pattern";

            assertThat(x.validate(value))
                    .startsWith(msg);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> x.checkValidity(value))
                    .withMessageStartingWith(msg);
        });

        assertThat("hello world").satisfies(value -> {
            String msg = "Expecting DataflowRef 'hello world' to match pattern";

            assertThat(x.validate(value))
                    .isNotNull()
                    .startsWith(msg);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> x.checkValidity(value))
                    .withMessageStartingWith(msg);
        });

        assertThat("hello").satisfies(value -> {
            assertThat(x.validate(value))
                    .isNull();

            assertThatCode(() -> x.checkValidity(value))
                    .doesNotThrowAnyException();
        });
    }

    @Test
    public void testOnAll() {
        assertThatNullPointerException()
                .isThrownBy(() -> Validator.onAll(null));
    }
}
