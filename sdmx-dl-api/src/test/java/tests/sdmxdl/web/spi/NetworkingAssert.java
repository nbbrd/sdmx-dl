package tests.sdmxdl.web.spi;

import lombok.NonNull;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.Networking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.web.spi.Networking.NETWORKING_PROPERTY_PREFIX;
import static tests.sdmxdl.api.TckUtil.SCREAMING_SNAKE_CASE;
import static tests.sdmxdl.api.TckUtil.startingWith;

@lombok.experimental.UtilityClass
public class NetworkingAssert {

    @SuppressWarnings("DataFlowIssue")
    public static void assertCompliance(@NonNull Networking x) {
        assertThat(x.getNetworkingId())
                .containsPattern(SCREAMING_SNAKE_CASE);

        assertThat(x.getNetworkingProperties())
                .are(startingWith(NETWORKING_PROPERTY_PREFIX))
                .doesNotHaveDuplicates();

        assertThatNullPointerException()
                .isThrownBy(() -> x.getNetwork(null, null, null));

        SdmxWebSource validSource = SdmxWebSource
                .builder()
                .id("valid")
                .driver("SDMX21")
                .endpointOf("http://localhost")
                .build();

        assertThat(x.getNetwork(validSource, null, null))
                .isNotNull()
                .satisfies(NetworkAssert::assertCompliance);
    }
}
