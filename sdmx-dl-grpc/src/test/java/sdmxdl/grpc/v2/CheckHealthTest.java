package sdmxdl.grpc.v2;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import sdmxdl.format.protobuf.web.HealthCheckDto;
import sdmxdl.format.protobuf.web.HealthReportDto;

/**
 * Checks the health operation in gRPC, REST and MCP.
 */
@QuarkusTest
public class CheckHealthTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    @GrpcClient
    SdmxWebManager grpc;

    @Test
    public void grpcMonitorByDefault() {
        assertThat(grpcHealth(WebHealthRequestDto.newBuilder().addSources("BBK")))
                .singleElement()
                .satisfies(report -> {
                    assertThat(report.getSource()).isEqualTo("BBK");
                    assertThat(report.getVerdict()).isEqualTo(HealthReportDto.Verdict.UNKNOWN);
                    assertThat(report.getMonitorError()).isEqualTo("No monitor defined");
                    assertThat(report.hasAccess()).isFalse();
                });
    }

    @Test
    public void grpcAccess() {
        assertThat(grpcHealth(WebHealthRequestDto.newBuilder().addSources("BBK").addChecks(HealthCheckDto.ACCESS)))
                .singleElement()
                .satisfies(report -> {
                    assertThat(report.getVerdict()).isEqualTo(HealthReportDto.Verdict.OK);
                    assertThat(report.hasMonitor()).isFalse();
                    assertThat(report.getAccess().getReachable()).isTrue();
                    assertThat(report.getAccess().getAccessible()).isTrue();
                });
    }

    @Test
    public void grpcAllSources() {
        assertThat(grpcHealth(WebHealthRequestDto.newBuilder()))
                .extracting(HealthReportDto::getSource)
                .contains("BBK", "ECB", "ESTAT")
                .isSorted();
    }

    @Test
    public void restSource() {
        given().when()
                .get("/sdmx-dl/v2/BBK/health")
                .then()
                .statusCode(200)
                .body("source", equalTo("BBK"))
                .body("verdict", equalTo("UNKNOWN"))
                .body("monitorError", equalTo("No monitor defined"));
    }

    @Test
    public void restSources() {
        given().queryParam("sources", "BBK,ECB")
                .queryParam("checks", "access")
                .when()
                .get("/sdmx-dl/v2/health")
                .then()
                .statusCode(200)
                .body("source", contains("BBK", "ECB"))
                .body("verdict", contains("OK", "OK"));
    }

    @Test
    public void mcpCheckHealth() {
        try (McpAssured.McpStreamableTestClient client = McpAssured.newConnectedStreamableClient()) {
            client.when()
                    .toolsCall("checkHealth", Map.of("source", "BBK"), r -> {
                        assertThat(r.isError()).isFalse();
                        assertThat(r.content())
                                .singleElement()
                                .extracting(content -> ((TextContent) content).text())
                                .asString()
                                .contains("\"verdict\": \"UNKNOWN\"", "No monitor defined");
                    })
                    .thenAssertResults();
        }
    }

    private List<HealthReportDto> grpcHealth(WebHealthRequestDto.Builder request) {
        return grpc.checkHealth(request.build()).collect().asList().await().atMost(TIMEOUT);
    }
}
