package sdmxdl.tck;

import internal.sdmxdl.tck.TckUtil;
import org.junit.jupiter.api.Test;
import sdmxdl.DataFilter;
import sdmxdl.Key;
import sdmxdl.ext.spi.SdmxDialect;
import sdmxdl.repo.SdmxRepositoryManager;
import sdmxdl.samples.RepoSamples;
import sdmxdl.tck.ext.MockedDialect;
import sdmxdl.tck.web.MockedWebDriver;
import sdmxdl.tck.web.SdmxWebListenerAssert;
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebDriver;

import java.net.URI;

public class ApiTest {

    @Test
    public void testSdmxRepositoryManager() {
        SdmxManagerAssert.assertCompliance(
                SdmxRepositoryManager.builder().repository(RepoSamples.REPO).build(),
                SdmxManagerAssert.Sample
                        .builder()
                        .validName("repoName")
                        .invalidName("ko")
                        .build()
        );
    }

    @Test
    public void testSdmxWebManager() {
        URI endpoint = TckUtil.asURI("http://" + RepoSamples.REPO.getName());
        SdmxWebSource source = SdmxWebSource
                .builder()
                .name("repoSource")
                .driver("repoDriver")
                .dialect("azerty")
                .endpoint(endpoint)
                .build();
        SdmxWebDriver driver = MockedWebDriver
                .builder()
                .name("repoDriver")
                .rank(0)
                .available(true)
                .repo(endpoint, RepoSamples.REPO)
                .source(source)
                .build();
        SdmxDialect dialect = new MockedDialect("azerty");
        SdmxManagerAssert.assertCompliance(
                SdmxWebManager.builder().driver(driver).dialect(dialect).build(),
                SdmxManagerAssert.Sample
                        .builder()
                        .validName("repoSource")
                        .invalidName("ko")
                        .build()
        );
    }

    @Test
    public void testSdmxFileManager() {
//        SdmxManagerAssert.assertCompliance(SdmxFileManager.builder().build());
    }

    @Test
    @SuppressWarnings("null")
    public void testDataSetCursor() {
        DataCursorAssert.assertCompliance(
                () -> RepoSamples.DATA_SET.getDataCursor(Key.ALL, DataFilter.FULL),
                Key.ALL, DataFilter.FULL
        );
    }

    @Test
    public void testNoOpWebListener() {
        SdmxWebListenerAssert.assertCompliance(
                SdmxWebListener.noOp()
        );
    }
}
