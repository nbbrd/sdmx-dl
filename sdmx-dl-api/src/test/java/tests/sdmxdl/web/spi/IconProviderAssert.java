package tests.sdmxdl.web.spi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import internal.sdmxdl.web.SourceIcons;
import internal.sdmxdl.web.spi.IconProviderLoader;
import java.io.IOException;
import java.util.Collections;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import sdmxdl.web.spi.IconProvider;
import tests.sdmxdl.api.ExtensionPoint;
import tests.sdmxdl.api.TckUtil;

@SuppressWarnings("ConstantConditions")
@lombok.experimental.UtilityClass
public class IconProviderAssert {

    @MightBeGenerated
    private static final ExtensionPoint<IconProvider> EXTENSION_POINT = ExtensionPoint.<IconProvider>builder()
            .id(IconProvider::getIconProviderId)
            .idPattern(IconProviderLoader.ID_PATTERN)
            .rank(IconProvider::getIconProviderRank)
            .rankLowerBound(IconProvider.UNKNOWN_ICON_PROVIDER_RANK)
            .properties(ignore -> Collections.emptyList())
            .propertiesPrefix("")
            .build();

    public void assertCompliance(@NonNull IconProvider provider) throws IOException {
        TckUtil.run(s -> EXTENSION_POINT.assertCompliance(s, provider));

        assertThat(provider.getIconNames())
                .doesNotHaveDuplicates()
                .allMatch(name -> SourceIcons.ICON_NAME_PATTERN.matcher(name).matches(), "valid icon name");

        assertThat(provider.getIconNames())
                .allSatisfy(name -> assertThat(provider.getIcons(name)).isNotEmpty());

        assertThat(provider.getIcons("not-a-known-icon-name")).isEmpty();

        assertThatNullPointerException().isThrownBy(() -> provider.getIcons(null));
    }
}
