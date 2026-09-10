package sdmxdl;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("DataFlowIssue")
public class DatabaseRefTest {

    @Test
    public void testParse() {
        assertThat(DatabaseRef.parse(""))
                .isEqualTo(DatabaseRef.NO_DATABASE)
                .hasToString(DatabaseRef.NO_DATABASE_KEYWORD);

        assertThat(DatabaseRef.parse("MAIN")).hasToString("MAIN");

        assertThatNullPointerException().isThrownBy(() -> DatabaseRef.parse(null));
    }

    @Test
    public void testCompareTo() {
        DatabaseRef empty = DatabaseRef.parse("");
        DatabaseRef alpha = DatabaseRef.parse("A");
        DatabaseRef beta = DatabaseRef.parse("B");

        assertThat(empty).isEqualByComparingTo(DatabaseRef.NO_DATABASE);
        assertThat(empty).isLessThan(alpha);
        assertThat(alpha).isLessThan(beta);
        assertThat(beta).isGreaterThan(alpha);
    }
}
