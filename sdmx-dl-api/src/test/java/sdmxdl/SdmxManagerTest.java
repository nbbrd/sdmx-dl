package sdmxdl;

import _test.sdmxdl.TestConnection;
import org.junit.jupiter.api.Test;
import sdmxdl.web.WebSource;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static tests.sdmxdl.api.RepoSamples.*;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("DataFlowIssue")
public class SdmxManagerTest {

    @Test
    public void testUsing() {
        SdmxManager<WebSource> manager = validManager();

        assertThatNullPointerException()
                .isThrownBy(() -> manager.using(null));

        assertThat(manager.using(BASIC_SOURCE).getSource())
                .isEqualTo(BASIC_SOURCE);
    }

    @Test
    public void testTestConnection() throws IOException {
        assertThat(validProvider().testConnection(SourceRequest.builder().build()))
                .contains(URI.create("http://localhost"));
    }

    @Test
    public void testGetSupportedFeatures() throws IOException {
        assertThat(validProvider().getSupportedFeatures(SourceRequest.builder().build()))
                .isEmpty();
    }

    @Test
    public void testGetDatabases() throws IOException {
        assertThat(validProvider().getDatabases(SourceRequest.builder().build()))
                .isEqualTo(REPO.getDatabases());
    }

    @Test
    public void testGetFlows() throws IOException {
        assertThat(validProvider().getFlows(DatabaseRequest.builder().build()))
                .isEqualTo(REPO.getFlows());
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
            public Connection getConnection(WebSource source, Languages languages) {
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



