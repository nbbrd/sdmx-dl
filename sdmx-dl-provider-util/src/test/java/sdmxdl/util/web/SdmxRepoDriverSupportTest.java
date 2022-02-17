package sdmxdl.util.web;

import org.junit.jupiter.api.Test;
import sdmxdl.tck.web.SdmxWebDriverAssert;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.samples.RepoSamples.REPO;

public class SdmxRepoDriverSupportTest {

    @Test
    public void testCompliance() {
        SdmxWebDriverAssert.assertCompliance(
                SdmxRepoDriverSupport
                        .builder()
                        .name("REPO")
                        .repo(REPO)
                        .build()
        );
    }

    @Test
    public void testGetDefaultSources() {
        assertThat(SdmxRepoDriverSupport.builder().name("REPO").build().getDefaultSources())
                .isEmpty();

        assertThat(SdmxRepoDriverSupport.builder().name("REPO").repo(REPO).build().getDefaultSources())
                .containsExactly(SdmxWebSource.builder().name(REPO.getName()).driver("REPO").endpointOf(REPO.getName()).build());
    }

    @Test
    public void testConnect() {
        SdmxWebSource source = SdmxWebSource.builder().name(REPO.getName()).driver("REPO").endpointOf(REPO.getName()).build();
        SdmxWebContext context = SdmxWebContext.builder().build();

        assertThatIOException()
                .isThrownBy(() -> SdmxRepoDriverSupport.builder().name("REPO").build().connect(source, context));

        assertThatNoException()
                .isThrownBy(() -> SdmxRepoDriverSupport.builder().name("REPO").repo(REPO).build().connect(source, context));
    }
}
