package tests.sdmxdl.web.spi;

import internal.sdmxdl.file.spi.ReaderLoader;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import sdmxdl.web.spi.Registry;
import tests.sdmxdl.api.ExtensionPoint;
import tests.sdmxdl.api.TckUtil;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.web.spi.Registry.REGISTRY_PROPERTY_PREFIX;

@lombok.experimental.UtilityClass
public class RegistryAssert {

    @MightBeGenerated
    private static final ExtensionPoint<Registry> EXTENSION_POINT = ExtensionPoint
            .<Registry>builder()
            .id(Registry::getRegistryId)
            .idPattern(ReaderLoader.ID_PATTERN)
            .rank(Registry::getRegistryRank)
            .rankLowerBound(Registry.UNKNOWN_REGISTRY_RANK)
            .properties(Registry::getRegistryProperties)
            .propertiesPrefix(REGISTRY_PROPERTY_PREFIX)
            .build();

    @SuppressWarnings("DataFlowIssue")
    public static void assertCompliance(@NonNull Registry registry) {
        TckUtil.run(s -> EXTENSION_POINT.assertCompliance(s, registry));

        assertThatNullPointerException()
                .isThrownBy(() -> registry.getSources(null, null, null));

        assertThat(registry.getSources(emptyList(), null, null))
                .isNotNull();
    }
}
