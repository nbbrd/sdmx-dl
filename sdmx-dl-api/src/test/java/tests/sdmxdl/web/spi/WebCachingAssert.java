package tests.sdmxdl.web.spi;

import lombok.NonNull;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebCaching;
import tests.sdmxdl.ext.CacheAssert;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.web.spi.WebCaching.WEB_CACHING_PROPERTY_PREFIX;
import static tests.sdmxdl.api.TckUtil.SCREAMING_SNAKE_CASE;
import static tests.sdmxdl.api.TckUtil.startingWith;

@lombok.experimental.UtilityClass
public class WebCachingAssert {

    @SuppressWarnings("DataFlowIssue")
    public static void assertWebCompliance(@NonNull WebCaching caching) {
        assertThat(caching.getWebCachingId())
                .containsPattern(SCREAMING_SNAKE_CASE)
                .isNotBlank();

        assertThat(caching.getWebCachingProperties())
                .are(startingWith(WEB_CACHING_PROPERTY_PREFIX))
                .doesNotHaveDuplicates();

        assertThatNullPointerException()
                .isThrownBy(() -> caching.getMonitorCache(null, emptyList(), null, null));

        assertThatNullPointerException()
                .isThrownBy(() -> caching.getDriverCache(null, emptyList(), null, null));

        WebSource validSource = WebSource
                .builder()
                .id("valid")
                .driver("SDMX21")
                .endpointOf("http://localhost")
                .build();

        assertThatNullPointerException()
                .isThrownBy(() -> caching.getMonitorCache(validSource, null, null, null));

        assertThatNullPointerException()
                .isThrownBy(() -> caching.getDriverCache(validSource, null, null, null));

        assertThat(caching.getMonitorCache(validSource, emptyList(), null, null))
                .isNotNull()
                .satisfies(CacheAssert::assertMonitorCompliance);

        assertThat(caching.getDriverCache(validSource, emptyList(), null, null))
                .isNotNull()
                .satisfies(CacheAssert::assertRepositoryCompliance);
    }
}
