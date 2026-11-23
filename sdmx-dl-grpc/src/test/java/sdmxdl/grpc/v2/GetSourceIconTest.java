package sdmxdl.grpc.v2;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import sdmxdl.grpc.MockedSdmxWebManagerProducer;

/**
 * Checks the source icon operation in gRPC and REST.
 */
@QuarkusTest
public class GetSourceIconTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    @GrpcClient
    SdmxWebManager grpc;

    @Test
    public void grpcDeclaredIcon() {
        assertThat(grpcIcon(WebSourceIconRequestDto.newBuilder().setSource("ECB")))
                .satisfies(icon -> {
                    assertThat(icon.getMediaType()).isEqualTo("image/png");
                    assertThat(icon.getSize()).isEqualTo(32);
                    assertThat(icon.getContent().toByteArray()).isEqualTo(MockedSdmxWebManagerProducer.ECB_ICON_PNG);
                });
    }

    @Test
    public void grpcFallback() {
        assertThat(grpcIcon(WebSourceIconRequestDto.newBuilder().setSource("BBK")))
                .satisfies(icon -> {
                    assertThat(icon.getMediaType()).isEqualTo("image/svg+xml");
                    assertThat(icon.hasSize()).isFalse();
                    assertThat(icon.getContent().toStringUtf8()).contains("<svg");
                });

        assertThat(grpcIcon(
                        WebSourceIconRequestDto.newBuilder().setSource("ECB").setFormat("svg")))
                .extracting(SourceIconDto::getMediaType)
                .isEqualTo("image/svg+xml");
    }

    @Test
    public void grpcErrors() {
        assertThatThrownBy(() -> grpcIcon(WebSourceIconRequestDto.newBuilder().setSource("UNKNOWN")))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND));
        assertThatThrownBy(() -> grpcIcon(
                        WebSourceIconRequestDto.newBuilder().setSource("BBK").setFormat("png")))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND));
        assertThatThrownBy(() -> grpcIcon(
                        WebSourceIconRequestDto.newBuilder().setSource("ECB").setFormat("gif")))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT));
        assertThatThrownBy(() -> grpcIcon(
                        WebSourceIconRequestDto.newBuilder().setSource("ECB").setSize(0)))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT));
    }

    @Test
    public void restDeclaredIcon() {
        byte[] content = given().queryParam("size", 16)
                .when()
                .get("/sdmx-dl/v2/ECB/icon")
                .then()
                .statusCode(200)
                .contentType("image/png")
                .header("ETag", notNullValue())
                .header("Cache-Control", containsString("max-age=86400"))
                .header("X-Content-Type-Options", equalTo("nosniff"))
                .header("Content-Security-Policy", containsString("sandbox"))
                .extract()
                .asByteArray();
        assertThat(content).isEqualTo(MockedSdmxWebManagerProducer.ECB_ICON_PNG);
    }

    @Test
    public void restFallback() {
        given().when()
                .get("/sdmx-dl/v2/BBK/icon")
                .then()
                .statusCode(200)
                .contentType("image/svg+xml")
                .body(containsString("<svg"));
    }

    @Test
    public void restNotModified() {
        String tag = given().when().get("/sdmx-dl/v2/ECB/icon").then().extract().header("ETag");
        given().header("If-None-Match", tag)
                .when()
                .get("/sdmx-dl/v2/ECB/icon")
                .then()
                .statusCode(304)
                .header("ETag", equalTo(tag));
    }

    @Test
    public void restErrors() {
        given().when().get("/sdmx-dl/v2/UNKNOWN/icon").then().statusCode(404).contentType("application/problem+json");
        given().queryParam("format", "gif")
                .when()
                .get("/sdmx-dl/v2/ECB/icon")
                .then()
                .statusCode(400)
                .contentType("application/problem+json");
    }

    private SourceIconDto grpcIcon(WebSourceIconRequestDto.Builder request) {
        return grpc.getSourceIcon(request.build()).await().atMost(TIMEOUT);
    }
}
