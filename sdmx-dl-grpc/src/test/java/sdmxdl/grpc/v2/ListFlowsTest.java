package sdmxdl.grpc.v2;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.rpc.ErrorInfo;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import sdmxdl.format.protobuf.FlowDto;

/**
 * Checks that flow descriptions can be cleaned and truncated, that flows can be paged and that errors are mapped
 * consistently in gRPC and REST.
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
                .body("items.description", contains("Exchange rate statistics"))
                .body("total", equalTo(1));
    }

    @Test
    public void restDescriptionIsTruncated() {
        given().queryParam("plainText", true)
                .queryParam("truncate", 10)
                .when()
                .get("/sdmx-dl/v2/BBK/flows")
                .then()
                .statusCode(200)
                .body("items.description", contains("Exchange…"));
    }

    @Test
    public void grpcPaging() {
        ListFlowsResponseDto page = grpc.listFlows(WebFlowsRequestDto.newBuilder()
                        .setSource("ECB")
                        .setMaxResults(2)
                        .setOffset(1)
                        .build())
                .await()
                .atMost(TIMEOUT);
        assertThat(page.getItemsList()).extracting(FlowDto::getRef).containsExactly("ECB,EXR,1.0", "ECB,ICP,1.0");
        assertThat(page.getTotal()).isEqualTo(4);

        ListFlowsResponseDto beyond = grpc.listFlows(WebFlowsRequestDto.newBuilder()
                        .setSource("ECB")
                        .setOffset(10)
                        .build())
                .await()
                .atMost(TIMEOUT);
        assertThat(beyond.getItemsList()).isEmpty();
        assertThat(beyond.getTotal()).isEqualTo(4);
    }

    @Test
    public void grpcErrors() {
        assertThatThrownBy(() -> grpcFlows(WebFlowsRequestDto.newBuilder().setSource("UNKNOWN")))
                .isInstanceOfSatisfying(StatusRuntimeException.class, ex -> {
                    assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
                    assertThat(getErrorInfo(ex)).satisfies(info -> {
                        assertThat(info.getReason()).isEqualTo("RESOURCE_NOT_FOUND");
                        assertThat(info.getDomain()).isEqualTo("sdmx-dl");
                        assertThat(info.getMetadataMap()).containsEntry("source", "UNKNOWN");
                    });
                });
        assertThatThrownBy(() -> grpcFlows(
                        WebFlowsRequestDto.newBuilder().setSource("ECB").setOffset(-1)))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT));
    }

    @Test
    public void restPaging() {
        given().queryParam("maxResults", 2)
                .queryParam("offset", 1)
                .when()
                .get("/sdmx-dl/v2/ECB/flows")
                .then()
                .statusCode(200)
                .body("items.ref", contains("ECB,EXR,1.0", "ECB,ICP,1.0"))
                .body("total", equalTo(4));
    }

    @Test
    public void restErrors() {
        given().when()
                .get("/sdmx-dl/v2/UNKNOWN/flows")
                .then()
                .statusCode(404)
                .contentType("application/problem+json")
                .body("status", equalTo(404))
                .body("title", equalTo("Not Found"))
                .body("reason", equalTo("RESOURCE_NOT_FOUND"))
                .body("source", equalTo("UNKNOWN"))
                .body("detail", containsString("UNKNOWN"));

        given().queryParam("offset", -1)
                .when()
                .get("/sdmx-dl/v2/ECB/flows")
                .then()
                .statusCode(400)
                .contentType("application/problem+json")
                .body("reason", equalTo("INVALID_ARGUMENT"));
    }

    private List<FlowDto> grpcFlows(WebFlowsRequestDto.Builder request) {
        return grpc.listFlows(request.build()).await().atMost(TIMEOUT).getItemsList();
    }

    private static ErrorInfo getErrorInfo(StatusRuntimeException ex) {
        try {
            return StatusProto.fromThrowable(ex).getDetails(0).unpack(ErrorInfo.class);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
