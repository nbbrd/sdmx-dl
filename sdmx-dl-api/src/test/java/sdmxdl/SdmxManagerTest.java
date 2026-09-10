package sdmxdl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static tests.sdmxdl.api.RepoSamples.*;

import _test.sdmxdl.TestConnection;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import sdmxdl.web.WebSource;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("DataFlowIssue")
public class SdmxManagerTest {

    @Test
    public void testUsing() {
        SdmxManager<WebSource> manager = validManager();

        assertThatNullPointerException().isThrownBy(() -> manager.using(null));

        assertThat(manager.using(BASIC_SOURCE).getSource()).isEqualTo(BASIC_SOURCE);
    }

    static SdmxManager<WebSource> validManager() {
        return new SdmxManager<WebSource>() {
            @Override
            public @NonNull Connection getConnection(@NonNull WebSource source, @NonNull Languages languages) {
                return TestConnection.TEST_VALID;
            }

            @Override
            public Function<? super WebSource, EventListener> getOnEvent() {
                return null;
            }

            @Override
            public Function<? super WebSource, ErrorListener> getOnError() {
                return null;
            }
        };
    }
}
