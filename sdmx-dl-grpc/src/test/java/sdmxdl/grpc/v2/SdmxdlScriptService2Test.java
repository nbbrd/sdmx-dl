package sdmxdl.grpc.v2;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;

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
