package sdmxdl.grpc.v2;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Checks the search of flows across sources in gRPC, REST and MCP.
 */
@QuarkusTest
public class SearchFlowsTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    @GrpcClient
    SdmxWebManager grpc;

    @Test
    public void grpcAllSourcesSortedAndPaged() {
        SearchFlowsResponseDto page = grpcSearch(
                WebSearchFlowsRequestDto.newBuilder().setMaxResults(2).setOffset(1));
        assertThat(page.getItemsList())
                .extracting(FlowEntryDto::getSource, o -> o.getFlow().getRef(), FlowEntryDto::hasDatabase)
                .containsExactly(tuple("ECB", "ECB,BSI,1.0", false), tuple("ECB", "ECB,EXR,1.0", false));
        assertThat(page.getTotal()).isEqualTo(7);
        assertThat(page.getFailuresList()).isEmpty();
    }

    @Test
    public void grpcQueryAndSources() {
        assertThat(grpcSearch(WebSearchFlowsRequestDto.newBuilder().setQuery("unemployment"))
                        .getItemsList())
                .first()
                .returns("ESTAT", FlowEntryDto::getSource)
                .returns("ESTAT,UNE_RT_M,1.0", o -> o.getFlow().getRef());

        assertThat(grpcSearch(WebSearchFlowsRequestDto.newBuilder()
                                .addSources("BBK")
                                .addSources("ESTAT")
                                .setTimeout(5000))
                        .getItemsList())
                .extracting(FlowEntryDto::getSource)
                .containsExactly("BBK", "ESTAT", "ESTAT");
    }

    @Test
    public void grpcErrors() {
        assertThatThrownBy(
                        () -> grpcSearch(WebSearchFlowsRequestDto.newBuilder().addSources("UNKNOWN")))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND));
        assertThatThrownBy(
                        () -> grpcSearch(WebSearchFlowsRequestDto.newBuilder().setTimeout(-1)))
                .isInstanceOfSatisfying(
                        StatusRuntimeException.class,
                        ex -> assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT));
    }

    @Test
    public void restSearch() {
        given().queryParam("sources", "BBK,ECB")
                .queryParam("query", "exchange")
                .queryParam("maxResults", 2)
                .when()
                .get("/sdmx-dl/v2/flows")
                .then()
                .statusCode(200)
                .body("items.source", containsInAnyOrder("BBK", "ECB"))
                .body("items.flow.ref", containsInAnyOrder("BBK,BBEX3,1.0", "ECB,EXR,1.0"))
                .body("failures", nullValue());

        given().queryParam("plainText", true)
                .queryParam("truncate", 10)
                .queryParam("maxResults", 1)
                .when()
                .get("/sdmx-dl/v2/flows")
                .then()
                .statusCode(200)
                .body("items.flow.description", contains("Exchange…"))
                .body("total", equalTo(7));
    }

    @Test
    public void restErrors() {
        given().queryParam("sources", "UNKNOWN")
                .when()
                .get("/sdmx-dl/v2/flows")
                .then()
                .statusCode(404)
                .contentType("application/problem+json")
                .body("reason", equalTo("RESOURCE_NOT_FOUND"));
    }

    @Test
    public void mcpSearchFlows() {
        try (McpAssured.McpStreamableTestClient client = McpAssured.newConnectedStreamableClient()) {
            client.when()
                    .toolsCall("searchFlows", Map.of("query", "unemployment"), r -> {
                        assertThat(r.isError()).isFalse();
                        assertThat(r.content())
                                .singleElement()
                                .extracting(content -> ((TextContent) content).text())
                                .asString()
                                .contains("\"source\": \"ESTAT\"", "UNE_RT_M");
                    })
                    .toolsCall(
                            "searchFlows",
                            Map.of("sources", List.of("UNKNOWN")),
                            r -> assertThat(r.isError()).isTrue())
                    .thenAssertResults();
        }
    }

    private SearchFlowsResponseDto grpcSearch(WebSearchFlowsRequestDto.Builder request) {
        return grpc.searchFlows(request.build()).await().atMost(TIMEOUT);
    }
}
