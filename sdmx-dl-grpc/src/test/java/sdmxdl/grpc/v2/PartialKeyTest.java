package sdmxdl.grpc.v2;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import sdmxdl.format.protobuf.CodelistDto;
import sdmxdl.format.protobuf.DataSetDto;
import sdmxdl.format.protobuf.SeriesDto;

/**
 * Checks that partial keys (fewer dimensions than the structure, e.g. {@code M.CHF} for
 * {@code M.CHF...}) are supported by every flavor that takes a key: gRPC, REST and MCP.
 */
@QuarkusTest
public class PartialKeyTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private static final String CHF = "M.CHF.EUR.SP00.A";
    private static final String USD = "M.USD.EUR.SP00.A";

    @GrpcClient
    SdmxWebManager grpc;

    // --- gRPC ---

    @Test
    public void grpcGetData() {
        assertThat(grpcData("M.CHF")).extracting(SeriesDto::getKey).containsExactly(CHF);
        assertThat(grpcData("M")).extracting(SeriesDto::getKey).containsExactlyInAnyOrder(CHF, USD);
    }

    @Test
    public void grpcGetDataStream() {
        assertThat(grpc.getDataStream(dataRequest("M.CHF"))
                        .collect()
                        .asList()
                        .await()
                        .atMost(TIMEOUT))
                .extracting(SeriesDto::getKey)
                .containsExactly(CHF);
    }

    @Test
    public void grpcListAvailability() {
        assertThat(grpcAvailability("M", "CURRENCY")).containsOnlyKeys("CHF", "USD");
        assertThat(grpcAvailability("M.CHF", "CURRENCY_DENOM")).containsOnlyKeys("EUR");
    }

    @Test
    public void grpcGenerateDataScript() {
        ScriptDto response = grpc.generateDataScript(WebDataScriptRequestDto.newBuilder()
                        .setRequest(dataRequest("M.CHF"))
                        .setOptions(ScriptOptionsDto.newBuilder().addCliLauncher("sdmx-dl"))
                        .build())
                .await()
                .atMost(TIMEOUT);
        assertThat(response.getContent()).contains("\"ECB\", \"EXR\", \"M.CHF\"");
    }

    // --- REST ---

    @Test
    public void restGetData() {
        given().queryParam("key", "M.CHF")
                .when()
                .get("/sdmx-dl/v2/ECB/EXR/data")
                .then()
                .statusCode(200)
                .body("data.key", containsInAnyOrder(CHF));

        given().queryParam("key", "M")
                .when()
                .get("/sdmx-dl/v2/ECB/EXR/data")
                .then()
                .statusCode(200)
                .body("data.key", containsInAnyOrder(CHF, USD));
    }

    @Test
    public void restGetDataStream() {
        given().urlEncodingEnabled(false)
                .queryParam("key", "M.CHF")
                .when()
                .get("/sdmx-dl/v2/ECB/EXR/data:stream")
                .then()
                .statusCode(200)
                .body("key", containsInAnyOrder(CHF));
    }

    @Test
    public void restListAvailability() {
        assertThat(restAvailability("M", "CURRENCY")).containsOnlyKeys("CHF", "USD");
        assertThat(restAvailability("M.CHF", "CURRENCY_DENOM")).containsOnlyKeys("EUR");
    }

    @Test
    public void restGenerateDataScript() {
        given().urlEncodingEnabled(false)
                .queryParam("key", "M.CHF")
                .queryParam("cliLauncher", "sdmx-dl")
                .when()
                .get("/sdmx-dl/v2/ECB/EXR/data:script")
                .then()
                .statusCode(200)
                .body("content", containsString("\"ECB\", \"EXR\", \"M.CHF\""));
    }

    // --- MCP ---

    @Test
    public void mcpGetData() {
        try (McpAssured.McpStreamableTestClient client = McpAssured.newConnectedStreamableClient()) {
            client.when()
                    .toolsCall("getData", Map.of("source", "ECB", "flow", "EXR", "key", "M.CHF"), r -> {
                        assertThat(r).returns(false, ToolResponse::isError);
                        assertThat(fromJson(DataSetDto.getDefaultInstance(), firstText(r))
                                        .getDataList())
                                .extracting(SeriesDto::getKey)
                                .containsExactly(CHF);
                    })
                    .toolsCall("getData", Map.of("source", "ECB", "flow", "EXR", "key", "M"), r -> {
                        assertThat(r).returns(false, ToolResponse::isError);
                        assertThat(fromJson(DataSetDto.getDefaultInstance(), firstText(r))
                                        .getDataList())
                                .extracting(SeriesDto::getKey)
                                .containsExactlyInAnyOrder(CHF, USD);
                    })
                    .thenAssertResults();
        }
    }

    @Test
    public void mcpListAvailability() {
        try (McpAssured.McpStreamableTestClient client = McpAssured.newConnectedStreamableClient()) {
            client.when()
                    .toolsCall(
                            "listAvailability",
                            Map.of("source", "ECB", "flow", "EXR", "key", "M", "dimension", "CURRENCY"),
                            r -> {
                                assertThat(r).returns(false, ToolResponse::isError);
                                assertThat(fromJson(CodelistDto.getDefaultInstance(), firstText(r))
                                                .getCodesMap())
                                        .containsOnlyKeys("CHF", "USD");
                            })
                    .toolsCall(
                            "listAvailability",
                            Map.of("source", "ECB", "flow", "EXR", "key", "M.CHF", "dimension", "CURRENCY_DENOM"),
                            r -> {
                                assertThat(r).returns(false, ToolResponse::isError);
                                assertThat(fromJson(CodelistDto.getDefaultInstance(), firstText(r))
                                                .getCodesMap())
                                        .containsOnlyKeys("EUR");
                            })
                    .thenAssertResults();
        }
    }

    @Test
    public void mcpGenerateDataScript() {
        try (McpAssured.McpStreamableTestClient client = McpAssured.newConnectedStreamableClient()) {
            client.when()
                    .toolsCall(
                            "generateDataScript",
                            Map.of("source", "ECB", "flow", "EXR", "key", "M.CHF", "cliLauncher", List.of("sdmx-dl")),
                            r -> {
                                assertThat(r).returns(false, ToolResponse::isError);
                                assertThat(fromJson(ScriptDto.getDefaultInstance(), firstText(r))
                                                .getContent())
                                        .contains("\"ECB\", \"EXR\", \"M.CHF\"");
                            })
                    .thenAssertResults();
        }
    }

    // --- Helpers ---

    private static WebDataRequestDto dataRequest(String key) {
        return WebDataRequestDto.newBuilder()
                .setSource("ECB")
                .setFlow("EXR")
                .setKey(key)
                .build();
    }

    private List<SeriesDto> grpcData(String key) {
        return grpc.getData(dataRequest(key)).await().atMost(TIMEOUT).getDataList();
    }

    private Map<String, String> grpcAvailability(String key, String dimension) {
        return grpc.listAvailability(WebAvailabilityRequestDto.newBuilder()
                        .setSource("ECB")
                        .setFlow("EXR")
                        .setKey(key)
                        .setDimension(dimension)
                        .build())
                .await()
                .atMost(TIMEOUT)
                .getCodesMap();
    }

    private static Map<String, String> restAvailability(String key, String dimension) {
        String json = given().queryParam("key", key)
                .when()
                .get("/sdmx-dl/v2/ECB/EXR/availability/" + dimension)
                .then()
                .statusCode(200)
                .extract()
                .asString();
        return fromJson(CodelistDto.getDefaultInstance(), json).getCodesMap();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Message> T fromJson(T defaultInstance, String json) {
        try {
            Message.Builder builder = defaultInstance.toBuilder();
            JsonFormat.parser().merge(json, builder);
            return (T) builder.build();
        } catch (InvalidProtocolBufferException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String firstText(ToolResponse response) {
        return response.content().get(0).asText().text();
    }
}
