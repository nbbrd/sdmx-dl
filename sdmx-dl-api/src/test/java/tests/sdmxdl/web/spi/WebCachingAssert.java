package tests.sdmxdl.web.spi;

import internal.sdmxdl.web.spi.WebCachingLoader;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebCaching;
import tests.sdmxdl.api.ExtensionPoint;
import tests.sdmxdl.api.TckUtil;
import tests.sdmxdl.ext.CacheAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@lombok.experimental.UtilityClass
public class WebCachingAssert {

    @MightBeGenerated
    private static final ExtensionPoint<WebCaching> EXTENSION_POINT = ExtensionPoint
            .<WebCaching>builder()
            .id(WebCaching::getWebCachingId)
            .idPattern(WebCachingLoader.ID_PATTERN)
            .rank(WebCaching::getWebCachingRank)
            .rankLowerBound(WebCaching.UNKNOWN_WEB_CACHING_RANK)
            .properties(WebCaching::getWebCachingProperties)
            .propertiesPrefix(WebCaching.WEB_CACHING_PROPERTY_PREFIX)
            .build();

    @SuppressWarnings("DataFlowIssue")
    public static void assertWebCompliance(@NonNull WebCaching caching) {
        TckUtil.run(s -> EXTENSION_POINT.assertCompliance(s, caching));

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
