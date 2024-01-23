package tests.sdmxdl.web.spi;

import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebCaching;
import tests.sdmxdl.api.ExtensionPoint;
import tests.sdmxdl.api.TckUtil;
import tests.sdmxdl.ext.CacheAssert;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static tests.sdmxdl.api.TckUtil.SCREAMING_SNAKE_CASE;

@lombok.experimental.UtilityClass
public class WebCachingAssert {

    @MightBeGenerated
    private static final ExtensionPoint<WebCaching> EXTENSION_POINT = ExtensionPoint
            .<WebCaching>builder()
            .id(WebCaching::getWebCachingId)
            .idPattern(SCREAMING_SNAKE_CASE)
            .rank(WebCaching::getWebCachingRank)
            .rankLowerBound(WebCaching.UNKNOWN_WEB_CACHING_RANK)
            .properties(WebCaching::getWebCachingProperties)
            .propertiesPrefix(WebCaching.WEB_CACHING_PROPERTY_PREFIX)
            .build();

    @SuppressWarnings("DataFlowIssue")
    public static void assertWebCompliance(@NonNull WebCaching caching) {
        TckUtil.run(s -> EXTENSION_POINT.assertCompliance(s, caching));

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
