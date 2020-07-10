package sdmxdl.tck;

import internal.sdmxdl.tck.TckUtil;
import org.junit.Test;
import sdmxdl.DataFilter;
import sdmxdl.Key;
import sdmxdl.ext.SdmxCache;
import sdmxdl.repo.SdmxRepositoryManager;
import sdmxdl.samples.RepoSamples;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebDriver;

import java.net.URL;

public class ApiTest {

    @Test
    public void testSdmxCaches() {
        SdmxCacheAssert.assertCompliance(SdmxCache.of());
        SdmxCacheAssert.assertCompliance(SdmxCache.noOp());
    }

    @Test
    public void testSdmxRepositoryManager() {
        SdmxManagerAssert.assertCompliance(
                SdmxRepositoryManager.builder().repository(RepoSamples.REPO).build(),
                SdmxManagerAssert.Sample
                        .builder()
                        .validName("test")
                        .invalidName("ko")
                        .build()
        );
    }

    @Test
    public void testSdmxWebManager() {
        URL endpoint = TckUtil.asURL("http://" + RepoSamples.REPO.getName());
        SdmxWebSource source = SdmxWebSource
                .builder()
                .name("repoSource")
                .driver("repoDriver")
                .endpoint(endpoint)
                .build();
        SdmxWebDriver driver = MockedWebDriver
                .builder()
                .name("repoDriver")
                .rank(0)
                .repo(endpoint, RepoSamples.REPO)
                .source(source)
                .build();
        SdmxManagerAssert.assertCompliance(
                SdmxWebManager.of(driver),
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
    public void testRepoDataCursor() {
        DataCursorAssert.assertCompliance(() -> RepoSamples.REPO.getDataCursor(RepoSamples.GOOD_FLOW_REF, Key.ALL, DataFilter.ALL).get());
    }
}
