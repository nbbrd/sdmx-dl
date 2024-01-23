package tests.sdmxdl.web.spi;

import lombok.NonNull;
import sdmxdl.web.spi.Registry;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.web.spi.Registry.REGISTRY_PROPERTY_PREFIX;
import static tests.sdmxdl.api.TckUtil.SCREAMING_SNAKE_CASE;
import static tests.sdmxdl.api.TckUtil.startingWith;

@lombok.experimental.UtilityClass
public class RegistryAssert {

    @SuppressWarnings("DataFlowIssue")
    public static void assertCompliance(@NonNull Registry registry) {
        assertThat(registry.getRegistryId())
                .containsPattern(SCREAMING_SNAKE_CASE)
                .isNotBlank();

        assertThat(registry.getRegistryProperties())
                .are(startingWith(REGISTRY_PROPERTY_PREFIX))
                .doesNotHaveDuplicates();

        assertThatNullPointerException()
                .isThrownBy(() -> registry.getSources(null, null, null));

        assertThat(registry.getSources(emptyList(), null, null))
                .isNotNull();
    }
}
