package sdmxdl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static tests.sdmxdl.api.RepoSamples.*;

import _test.sdmxdl.TestConnection;
import java.io.IOException;
import java.net.URI;
import java.util.Comparator;
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

    @Test
    public void testTestConnection() throws IOException {
        assertThat(validProvider().testConnection()).contains(URI.create("http://localhost"));
    }

    @Test
    public void testGetSupportedFeatures() throws IOException {
        assertThat(validProvider().getSupportedFeatures()).isEmpty();
    }

    @Test
    public void testListDatabases() throws IOException {
        assertThat(validProvider().listDatabases(SourceRequest.DEFAULT))
                .isSortedAccordingTo(Comparator.comparing(o -> o.getRef().toString()))
                .containsExactlyInAnyOrderElementsOf(REPO.getDatabases());
    }

    @Test
    public void testListFlows() throws IOException {
        assertThat(validProvider().listFlows(DatabaseRequest.DEFAULT))
                .isSortedAccordingTo(Comparator.comparing(o -> o.getRef().toString()))
                .containsExactlyInAnyOrderElementsOf(REPO.getFlows());
    }

    @Test
    public void testGetMeta() throws IOException {
        assertThat(validProvider().getMeta(FlowRequest.builder().flow(FLOW_REF).build()))
                .isEqualTo(META_SET);
    }

    @Test
    public void testGetData() throws IOException {
        assertThat(validProvider().getData(KeyRequest.builder().flow(FLOW_REF).build()))
                .isEqualTo(DATA_SET);
    }

    private static Provider<WebSource> validProvider() {
        return validManager().using(BASIC_SOURCE);
    }

    private static SdmxManager<WebSource> validManager() {
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
