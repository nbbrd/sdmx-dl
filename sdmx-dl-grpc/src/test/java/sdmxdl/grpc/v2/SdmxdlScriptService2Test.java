package sdmxdl.grpc.v2;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class SdmxdlScriptService2Test {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    @GrpcClient
    SdmxWebManager grpc;

    @Test
    public void grpcListScriptTargets() {
        List<ScriptTargetDto> response = grpc.listScriptTargets(EmptyDto.getDefaultInstance())
                .collect()
                .asList()
                .await()
                .atMost(TIMEOUT);
        assertThat(response).extracting(ScriptTargetDto::getTarget).contains("python/cli", "python/rest");
        assertThat(response)
                .filteredOn(ScriptTargetDto::getTarget, "python/cli")
                .singleElement()
                .satisfies(target -> assertThat(target.getCommandsList()).contains("data", "flows"));
    }

    @Test
    public void grpcGenerateDataScript() {
        ScriptDto response = grpc.generateDataScript(WebDataScriptRequestDto.newBuilder()
                        .setRequest(WebDataRequestDto.newBuilder()
                                .setSource("ECB")
                                .setFlow("EXR")
                                .setKey("M.CHF.EUR.SP00.A")
                                .setLastN(12))
                        .setOptions(ScriptOptionsDto.newBuilder().addCliLauncher("sdmx-dl.exe"))
                        .build())
                .await()
                .atMost(TIMEOUT);
        assertThat(response.getTarget()).isEqualTo("python/cli");
        assertThat(response.getFileExtension()).isEqualTo("py");
        assertThat(response.getContent())
                .contains(
                        "[\"sdmx-dl.exe\", \"fetch\", \"data\", \"ECB\", \"EXR\", \"M.CHF.EUR.SP00.A\", \"--last-n\", \"12\"]");
    }

    @Test
    public void grpcGenerateFlowsScript() {
        ScriptDto response = grpc.generateFlowsScript(WebFlowsScriptRequestDto.newBuilder()
                        .setRequest(WebFlowsRequestDto.newBuilder().setSource("ECB"))
                        .setOptions(ScriptOptionsDto.newBuilder()
                                .setTarget("python/rest")
                                .setRestEndpoint("http://example.org/api"))
                        .build())
                .await()
                .atMost(TIMEOUT);
        assertThat(response.getTarget()).isEqualTo("python/rest");
        assertThat(response.getContent()).contains("http://example.org/api/ECB/flows");
    }

    @Test
    public void grpcGenerateFlowsScriptWithDescriptionOptions() {
        ScriptDto response = grpc.generateFlowsScript(WebFlowsScriptRequestDto.newBuilder()
                        .setRequest(WebFlowsRequestDto.newBuilder()
                                .setSource("ECB")
                                .setPlainText(true)
                                .setTruncate(80))
                        .build())
                .await()
                .atMost(TIMEOUT);
        assertThat(response.getWarningsList()).isEmpty();
        assertThat(response.getContent())
                .contains("[\"sdmx-dl\", \"list\", \"flows\", \"ECB\", \"--plain-text\", \"--truncate\", \"80\"]");
    }

    @Test
    public void restGenerateFlowsScriptWithDescriptionOptions() {
        given().urlEncodingEnabled(false)
                .queryParam("target", "python/rest")
                .queryParam("plainText", true)
                .queryParam("truncate", 80)
                .when()
                .get("/sdmx-dl/v2/ECB/flows:script")
                .then()
                .statusCode(200)
                .body("content", containsString("{\"plainText\": \"true\", \"truncate\": 80}"));
    }

    @Test
    public void grpcGenerateScriptWithUnknownTarget() {
        assertThatThrownBy(() -> grpc.generateFlowsScript(WebFlowsScriptRequestDto.newBuilder()
                                .setRequest(WebFlowsRequestDto.newBuilder().setSource("ECB"))
                                .setOptions(ScriptOptionsDto.newBuilder().setTarget("cobol/cli"))
                                .build())
                        .await()
                        .atMost(TIMEOUT))
                .isNotNull();
    }

    @Test
    public void restListScriptTargets() {
        given().when()
                .get("/sdmx-dl/v2/script/targets")
                .then()
                .statusCode(200)
                .body("target", hasItems("python/cli", "python/rest"));
    }

    @Test
    public void restGenerateDataScript() {
        given().urlEncodingEnabled(false)
                .queryParam("key", "M.CHF.EUR.SP00.A")
                .queryParam("lastN", 12)
                .queryParam("cliLauncher", "java", "-jar", "sdmx-dl-cli-bin.jar")
                .when()
                .get("/sdmx-dl/v2/ECB/EXR/data:script")
                .then()
                .statusCode(200)
                .body("target", containsString("python/cli"))
                .body(
                        "content",
                        containsString(
                                "[\"java\", \"-jar\", \"sdmx-dl-cli-bin.jar\", \"fetch\", \"data\", \"ECB\", \"EXR\", \"M.CHF.EUR.SP00.A\", \"--last-n\", \"12\"]"));
    }

    @Test
    public void restGenerateFlowsScriptTargetsThisServerByDefault() {
        given().urlEncodingEnabled(false)
                .queryParam("target", "python/rest")
                .when()
                .get("/sdmx-dl/v2/ECB/flows:script")
                .then()
                .statusCode(200)
                .body("content", containsString("/sdmx-dl/v2/ECB/flows\""));
    }

    @Test
    public void grpcGenerateScriptWithUnsupportedProperty() {
        ScriptDto response = grpc.generateFlowsScript(WebFlowsScriptRequestDto.newBuilder()
                        .setRequest(WebFlowsRequestDto.newBuilder().setSource("ECB"))
                        .setOptions(ScriptOptionsDto.newBuilder().putProperties("sdmxdl.script.python.x", "y"))
                        .build())
                .await()
                .atMost(TIMEOUT);
        assertThat(response.getWarningsList()).contains("Unsupported property 'sdmxdl.script.python.x' was ignored");
    }

    @Test
    public void restGenerateScriptWithUnsupportedProperty() {
        given().urlEncodingEnabled(false)
                .queryParam("property", "sdmxdl.script.python.x=y")
                .when()
                .get("/sdmx-dl/v2/ECB/flows:script")
                .then()
                .statusCode(200)
                .body("warnings", hasItems("Unsupported property 'sdmxdl.script.python.x' was ignored"));
    }

    @Test
    public void restGenerateScriptWithInvalidProperty() {
        given().urlEncodingEnabled(false)
                .queryParam("property", "novalue")
                .when()
                .get("/sdmx-dl/v2/ECB/flows:script")
                .then()
                .statusCode(400)
                .body("message", containsString("novalue"));
    }

    @Test
    public void restGenerateScriptAsJsonByDefault() {
        given().urlEncodingEnabled(false)
                .queryParam("target", "r/rest")
                .when()
                .get("/sdmx-dl/v2/ECB/flows:script")
                .then()
                .statusCode(200)
                .contentType(startsWith("application/json"))
                .body("fileExtension", equalTo("R"))
                .body("mediaType", equalTo("text/x-r"));
    }

    @Test
    public void restGenerateScriptAsRawWithFormat() {
        given().urlEncodingEnabled(false)
                .queryParam("key", "M.CHF.EUR.SP00.A")
                .queryParam("target", "python/cli")
                .queryParam("format", "raw")
                .when()
                .get("/sdmx-dl/v2/ECB/EXR/data:script")
                .then()
                .statusCode(200)
                .contentType(startsWith("text/x-python"))
                .header("Content-Disposition", equalTo("attachment; filename=\"ECB_EXR_data.py\""))
                .body(startsWith("import contextlib"));
    }

    @Test
    public void restGenerateScriptAsRawWithAccept() {
        given().urlEncodingEnabled(false)
                .accept("text/plain")
                .queryParam("target", "powerquery/rest")
                .queryParam("outputFile", "flows.csv")
                .when()
                .get("/sdmx-dl/v2/ECB/flows:script")
                .then()
                .statusCode(200)
                .contentType(startsWith("text/plain"))
                .header("Content-Disposition", equalTo("attachment; filename=\"ECB_flows.pq\""))
                .header("Sdmxdl-Script-Warning", notNullValue());

        given().urlEncodingEnabled(false)
                .accept("application/octet-stream")
                .queryParam("target", "bash/cli")
                .when()
                .get("/sdmx-dl/v2/ECB/flows:script")
                .then()
                .statusCode(200)
                .contentType(startsWith("application/octet-stream"))
                .header("Content-Disposition", equalTo("attachment; filename=\"ECB_flows.sh\""));
    }

    @Test
    public void restGenerateScriptAsRawWithError() {
        given().urlEncodingEnabled(false)
                .accept("text/plain")
                .queryParam("target", "cobol/cli")
                .when()
                .get("/sdmx-dl/v2/ECB/flows:script")
                .then()
                .statusCode(400)
                .contentType(startsWith("application/json"))
                .body("message", containsString("cobol/cli"));
    }

    @Test
    public void restGenerateScriptWithUnknownTarget() {
        given().urlEncodingEnabled(false)
                .queryParam("target", "cobol/cli")
                .when()
                .get("/sdmx-dl/v2/ECB/flows:script")
                .then()
                .statusCode(400)
                .body("message", containsString("cobol/cli"));
    }
}
