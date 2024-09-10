package sdmxdl.provider.ri.caching;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.Confidentiality.*;
import static sdmxdl.provider.ri.caching.RiCaching.isForbidden;
import static tests.sdmxdl.file.spi.FileCachingAssert.assertFileCompliance;
import static tests.sdmxdl.web.spi.WebCachingAssert.assertWebCompliance;

public class RiCachingTest {

    @Test
    public void testCompliance() {
        RiCaching x = new RiCaching();
        assertFileCompliance(x);
        assertWebCompliance(x);
    }

    @Test
    public void testIsForbidden() {
        assertThat(isForbidden(o -> null, PUBLIC)).isFalse();
        assertThat(isForbidden(o -> null, UNRESTRICTED)).isFalse();
        assertThat(isForbidden(o -> null, RESTRICTED)).isFalse();
        assertThat(isForbidden(o -> null, CONFIDENTIAL)).isTrue();
        assertThat(isForbidden(o -> null, SECRET)).isTrue();

        assertThat(isForbidden(o -> RESTRICTED.name(), PUBLIC)).isFalse();
        assertThat(isForbidden(o -> RESTRICTED.name(), UNRESTRICTED)).isFalse();
        assertThat(isForbidden(o -> RESTRICTED.name(), RESTRICTED)).isFalse();
        assertThat(isForbidden(o -> RESTRICTED.name(), CONFIDENTIAL)).isTrue();
        assertThat(isForbidden(o -> RESTRICTED.name(), SECRET)).isTrue();

        assertThat(isForbidden(o -> UNRESTRICTED.name(), PUBLIC)).isFalse();
        assertThat(isForbidden(o -> UNRESTRICTED.name(), UNRESTRICTED)).isFalse();
        assertThat(isForbidden(o -> UNRESTRICTED.name(), RESTRICTED)).isTrue();
        assertThat(isForbidden(o -> UNRESTRICTED.name(), CONFIDENTIAL)).isTrue();
        assertThat(isForbidden(o -> UNRESTRICTED.name(), SECRET)).isTrue();

        assertThat(isForbidden(o -> CONFIDENTIAL.name(), PUBLIC)).isFalse();
        assertThat(isForbidden(o -> CONFIDENTIAL.name(), UNRESTRICTED)).isFalse();
        assertThat(isForbidden(o -> CONFIDENTIAL.name(), RESTRICTED)).isFalse();
        assertThat(isForbidden(o -> CONFIDENTIAL.name(), CONFIDENTIAL)).isFalse();
        assertThat(isForbidden(o -> CONFIDENTIAL.name(), SECRET)).isTrue();
    }
}
