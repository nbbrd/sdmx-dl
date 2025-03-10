package sdmxdl.grpc;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import sdmxdl.format.protobuf.web.WebSource;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class SdmxWebManagerServiceTest {

    @GrpcClient
    SdmxWebManager grpc;

    @Test
    public void testGetSources() {
        Empty request = Empty.newBuilder().build();
        List<WebSource> response = grpc.getSources(request).collect().asList().await().atMost(Duration.ofSeconds(5));
        assertThat(response)
                .hasSizeGreaterThanOrEqualTo(34)
                .extracting(WebSource::getId)
                .contains("ECB");
    }
}
