package tests.sdmxdl.web.spi;

import internal.sdmxdl.web.spi.NetworkingLoader;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Networking;
import tests.sdmxdl.api.ExtensionPoint;
import tests.sdmxdl.api.TckUtil;

import static org.assertj.core.api.Assertions.*;
import static sdmxdl.web.spi.Networking.NETWORKING_PROPERTY_PREFIX;

@lombok.experimental.UtilityClass
public class NetworkingAssert {

    @MightBeGenerated
    private static final ExtensionPoint<Networking> EXTENSION_POINT = ExtensionPoint
            .<Networking>builder()
            .id(Networking::getNetworkingId)
            .idPattern(NetworkingLoader.ID_PATTERN)
            .rank(Networking::getNetworkingRank)
            .rankLowerBound(Networking.UNKNOWN_NETWORKING_RANK)
            .properties(Networking::getNetworkingProperties)
            .propertiesPrefix(NETWORKING_PROPERTY_PREFIX)
            .build();

    @SuppressWarnings("DataFlowIssue")
    public static void assertCompliance(@NonNull Networking networking) {
        TckUtil.run(s -> EXTENSION_POINT.assertCompliance(s, networking));

        assertThatNullPointerException()
                .isThrownBy(() -> networking.getNetwork(null, null, null));

        WebSource validSource = WebSource
                .builder()
                .id("valid")
                .driver("SDMX21")
                .endpointOf("http://localhost")
                .build();

        assertThat(networking.getNetwork(validSource, null, null))
                .isNotNull()
                .satisfies(NetworkAssert::assertCompliance);

        assertThatCode(networking::warmupNetwork)
                .doesNotThrowAnyException();
    }
}
