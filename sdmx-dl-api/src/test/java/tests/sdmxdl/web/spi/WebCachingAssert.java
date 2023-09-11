package tests.sdmxdl.web.spi;

import lombok.NonNull;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebCaching;
import tests.sdmxdl.ext.CacheAssert;

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
                .isThrownBy(() -> caching.getMonitorCache(null, null, null));

        assertThatNullPointerException()
                .isThrownBy(() -> caching.getDriverCache(null, null, null));

        WebSource validSource = WebSource
                .builder()
                .id("valid")
                .driver("SDMX21")
                .endpointOf("http://localhost")
                .build();

        assertThat(caching.getMonitorCache(validSource, null, null))
                .isNotNull()
                .satisfies(CacheAssert::assertMonitorCompliance);

        assertThat(caching.getDriverCache(validSource, null, null))
                .isNotNull()
                .satisfies(CacheAssert::assertRepositoryCompliance);
    }
}
