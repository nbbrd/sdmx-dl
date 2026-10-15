package sdmxdl.grpc.v2;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import sdmxdl.format.protobuf.CodeDto;

/**
 * Checks that the availability dimension can be referenced by id, by zero-based index or left
 * empty (first wildcard dimension of the key) in every flavor: gRPC, REST and MCP.
 */
@QuarkusTest
public class ListAvailabilityTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    @GrpcClient
    SdmxWebManager grpc;

    // --- gRPC ---

    @Test
    public void grpcDimensionById() {
        assertThat(grpcAvailability("M", "CURRENCY")).satisfies(isCurrency());
    }

    @Test
    public void grpcDimensionByIndex() {
        assertThat(grpcAvailability("M", "1")).satisfies(isCurrency());
    }

    @Test
    public void grpcFirstWildcardDimension() {
        assertThat(grpcAvailability("M", "")).satisfies(isCurrency());
        assertThat(grpcAvailability("M..EUR", "")).satisfies(isCurrency());
        assertThat(grpcAvailability("M.CHF", "")).satisfies(isCurrencyDenom());
    }

    @Test
    public void grpcInvalidDimension() {
        assertThatThrownBy(() -> grpcAvailability("M.CHF", "CURRENCY"))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT))
                .hasMessageContaining("wildcard");
        assertThatThrownBy(() -> grpcAvailability("M", "UNKNOWN"))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND))
                .hasMessageContaining("Cannot find dimension");
    }

    // --- REST ---

    @Test
    public void restDimensionById() {
        assertThat(restAvailability("M", "CURRENCY")).satisfies(isCurrency());
    }

    @Test
    public void restDimensionByIndex() {
        assertThat(restAvailability("M", "1")).satisfies(isCurrency());
    }

    @Test
    public void restFirstWildcardDimension() {
        assertThat(restAvailability("M", "")).satisfies(isCurrency());
        assertThat(restAvailability("M.CHF", null)).satisfies(isCurrencyDenom());
    }

    @Test
    public void restInvalidDimension() {
        given().queryParam("key", "M.CHF")
                .queryParam("dimension", "CURRENCY")
                .when()
                .get("/sdmx-dl/v2/ECB/EXR/availability")
                .then()
                .statusCode(400)
                .contentType("application/problem+json")
                .body("status", equalTo(400))
                .body("reason", equalTo("INVALID_ARGUMENT"))
                .body("source", equalTo("ECB"))
                .body("detail", containsString("wildcard"));

        given().queryParam("key", "M")
                .queryParam("dimension", "UNKNOWN")
                .when()
                .get("/sdmx-dl/v2/ECB/EXR/availability")
                .then()
                .statusCode(404)
                .contentType("application/problem+json")
                .body("reason", equalTo("RESOURCE_NOT_FOUND"))
                .body("detail", containsString("Cannot find dimension"));
    }

    // --- MCP ---

    @Test
    public void mcpDimensionModes() {
        try (McpAssured.McpStreamableTestClient client = McpAssured.newConnectedStreamableClient()) {
            client.when()
                    .toolsCall(
                            "listAvailability",
                            mcpArgs("M", "CURRENCY"),
                            r -> assertThat(mcpResult(r)).satisfies(isCurrency()))
                    .toolsCall(
                            "listAvailability",
                            mcpArgs("M", "1"),
                            r -> assertThat(mcpResult(r)).satisfies(isCurrency()))
                    .toolsCall(
                            "listAvailability",
                            mcpArgs("M", ""),
                            r -> assertThat(mcpResult(r)).satisfies(isCurrency()))
                    .toolsCall(
                            "listAvailability",
                            mcpArgs("M.CHF", null),
                            r -> assertThat(mcpResult(r)).satisfies(isCurrencyDenom()))
                    .toolsCall(
                            "listAvailability",
                            mcpArgs("M.CHF", "CURRENCY"),
                            r -> assertThat(r).returns(true, ToolResponse::isError))
                    .thenAssertResults();
        }
    }

    // --- Helpers ---

    private static Consumer<ListAvailabilityResponseDto> isCurrency() {
        return dto -> {
            assertThat(dto.getDimension()).isEqualTo("CURRENCY");
            assertThat(dto.getItemsList()).extracting(CodeDto::getId).containsExactly("CHF", "USD");
            assertThat(dto.getItemsList()).extracting(CodeDto::getName).containsExactly("Swiss franc", "US dollar");
            assertThat(dto.getTotal()).isEqualTo(2);
        };
    }

    private static Consumer<ListAvailabilityResponseDto> isCurrencyDenom() {
        return dto -> {
            assertThat(dto.getDimension()).isEqualTo("CURRENCY_DENOM");
            assertThat(dto.getItemsList()).extracting(CodeDto::getId).containsExactly("EUR");
            assertThat(dto.getTotal()).isEqualTo(1);
        };
    }

    private ListAvailabilityResponseDto grpcAvailability(String key, String dimension) {
        return grpc.listAvailability(WebAvailabilityRequestDto.newBuilder()
                        .setSource("ECB")
                        .setFlow("EXR")
                        .setKey(key)
                        .setDimension(dimension)
                        .build())
                .await()
                .atMost(TIMEOUT);
    }

    private static ListAvailabilityResponseDto restAvailability(String key, String dimension) {
        var request = given().queryParam("key", key);
        if (dimension != null) {
            request = request.queryParam("dimension", dimension);
        }
        String json = request.when()
                .get("/sdmx-dl/v2/ECB/EXR/availability")
                .then()
                .statusCode(200)
                .extract()
                .asString();
        return fromJson(json);
    }

    private static Map<String, Object> mcpArgs(String key, String dimension) {
        Map<String, Object> result = new HashMap<>();
        result.put("source", "ECB");
        result.put("flow", "EXR");
        result.put("key", key);
        if (dimension != null) {
            result.put("dimension", dimension);
        }
        return result;
    }

    private static ListAvailabilityResponseDto mcpResult(ToolResponse response) {
        assertThat(response).returns(false, ToolResponse::isError);
        return fromJson(response.content().get(0).asText().text());
    }

    private static ListAvailabilityResponseDto fromJson(String json) {
        try {
            ListAvailabilityResponseDto.Builder builder = ListAvailabilityResponseDto.newBuilder();
            JsonFormat.parser().merge(json, builder);
            return builder.build();
        } catch (InvalidProtocolBufferException ex) {
            throw new RuntimeException(ex);
        }
    }
}
