package sdmxdl.grpc.v2;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import sdmxdl.format.protobuf.FlowDto;

/**
 * Checks that flow descriptions can be cleaned and truncated in gRPC and REST.
 */
@QuarkusTest
public class ListFlowsTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    @GrpcClient
    SdmxWebManager grpc;

    @Test
    public void grpcDescriptionIsKeptByDefault() {
        assertThat(grpcFlows(WebFlowsRequestDto.newBuilder().setSource("BBK")))
                .extracting(FlowDto::getDescription)
                .containsExactly("Exchange rate statistics");
    }

    @Test
    public void grpcDescriptionIsTruncated() {
        assertThat(grpcFlows(WebFlowsRequestDto.newBuilder()
                        .setSource("BBK")
                        .setPlainText(true)
                        .setTruncate(10)))
                .extracting(FlowDto::getDescription)
                .containsExactly("Exchange…");
    }

    @Test
    public void restDescriptionIsKeptByDefault() {
        given().when()
                .get("/sdmx-dl/v2/BBK/flows")
                .then()
                .statusCode(200)
                .body("description", contains("Exchange rate statistics"));
    }

    @Test
    public void restDescriptionIsTruncated() {
        given().queryParam("plainText", true)
                .queryParam("truncate", 10)
                .when()
                .get("/sdmx-dl/v2/BBK/flows")
                .then()
                .statusCode(200)
                .body("description", contains("Exchange…"));
    }

    private List<FlowDto> grpcFlows(WebFlowsRequestDto.Builder request) {
        return grpc.listFlows(request.build()).collect().asList().await().atMost(TIMEOUT);
    }
}
