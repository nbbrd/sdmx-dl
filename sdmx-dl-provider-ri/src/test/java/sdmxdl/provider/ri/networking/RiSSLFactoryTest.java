package sdmxdl.provider.ri.networking;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sdmxdl.provider.ri.networking.RiSSLFactory;

import java.util.stream.Stream;

import static tests.sdmxdl.web.spi.SSLFactoryAssert.assertCompliance;

public class RiSSLFactoryTest {

    @ParameterizedTest(name = "noDefaultTrustMaterial={0}, noSystemTrustMaterial={1}")
    @MethodSource("provideComplianceArgs")
    public void testCompliance(boolean noDefaultTrustMaterial, boolean noSystemTrustMaterial) {
        assertCompliance(RiSSLFactory
                .builder()
                .noDefaultTrustMaterial(noDefaultTrustMaterial)
                .noSystemTrustMaterial(noSystemTrustMaterial)
                .build()
        );
    }

    private static Stream<Arguments> provideComplianceArgs() {
        return Stream.of(
                Arguments.of(false, false),
                Arguments.of(false, true),
                Arguments.of(true, false),
                Arguments.of(true, true)
        );
    }
}
