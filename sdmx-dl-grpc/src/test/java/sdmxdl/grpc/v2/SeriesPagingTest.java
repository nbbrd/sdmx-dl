package sdmxdl.grpc.v2;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import sdmxdl.format.protobuf.DataSetDto;
import sdmxdl.format.protobuf.SeriesDto;

/**
 * Checks that the series of a data request can be paged with 'series_offset' and 'series_limit',
 * and that 'total' gives their number before paging, in every flavor: gRPC, REST and MCP.
 */
@QuarkusTest
public class SeriesPagingTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private static final String CHF = "M.CHF.EUR.SP00.A";
    private static final String USD = "M.USD.EUR.SP00.A";

    @GrpcClient
    SdmxWebManager grpc;

    @Test
    public void grpcGetData() {
        DataSetDto all = grpc.getData(dataRequest().build()).await().atMost(TIMEOUT);
        assertThat(all.getTotal()).isEqualTo(2);

        DataSetDto first = grpcData(0, 1);
        DataSetDto second = grpcData(1, 1);
        assertThat(first.getTotal()).isEqualTo(2);
        assertThat(second.getTotal()).isEqualTo(2);
        assertThat(keysOf(first, second)).containsExactlyInAnyOrder(CHF, USD);

        assertThat(grpcData(2, 1).getDataList()).isEmpty();
    }

    @Test
    public void grpcGetDataStream() {
        assertThat(grpc.getDataStream(dataRequest().setSeriesLimit(1).build())
                        .collect()
                        .asList()
                        .await()
                        .atMost(TIMEOUT))
                .hasSize(1);
    }

    @Test
    public void restGetData() {
        given().queryParam("key", "M")
                .queryParam("seriesOffset", 1)
                .queryParam("seriesLimit", 1)
                .when()
                .get("/sdmx-dl/v2/ECB/EXR/data")
                .then()
                .statusCode(200)
                .body("data", hasSize(1))
                .body("total", equalTo(2));

        given().queryParam("key", "M")
                .queryParam("seriesLimit", 0)
                .when()
                .get("/sdmx-dl/v2/ECB/EXR/data")
                .then()
                .statusCode(400);
    }

    @Test
    public void restGetDataStream() {
        given().urlEncodingEnabled(false)
                .queryParam("key", "M")
                .queryParam("seriesLimit", 1)
                .when()
                .get("/sdmx-dl/v2/ECB/EXR/data:stream")
                .then()
                .statusCode(200)
                .body("$", hasSize(1));
    }

    @Test
    public void mcpGetData() {
        try (McpAssured.McpStreamableTestClient client = McpAssured.newConnectedStreamableClient()) {
            client.when()
                    .toolsCall(
                            "getData",
                            Map.of("source", "ECB", "flow", "EXR", "key", "M", "seriesOffset", 1, "seriesLimit", 1),
                            r -> {
                                assertThat(r).returns(false, ToolResponse::isError);
                                DataSetDto data =
                                        fromJson(r.content().get(0).asText().text());
                                assertThat(data.getDataList()).hasSize(1);
                                assertThat(data.getTotal()).isEqualTo(2);
                            })
                    .thenAssertResults();
        }
    }

    private static WebDataRequestDto.Builder dataRequest() {
        return WebDataRequestDto.newBuilder().setSource("ECB").setFlow("EXR").setKey("M");
    }

    private DataSetDto grpcData(int seriesOffset, int seriesLimit) {
        return grpc.getData(dataRequest()
                        .setSeriesOffset(seriesOffset)
                        .setSeriesLimit(seriesLimit)
                        .build())
                .await()
                .atMost(TIMEOUT);
    }

    private static List<String> keysOf(DataSetDto... pages) {
        List<String> result = new ArrayList<>();
        for (DataSetDto page : pages) {
            page.getDataList().stream().map(SeriesDto::getKey).forEach(result::add);
        }
        return result;
    }

    private static DataSetDto fromJson(String json) {
        try {
            DataSetDto.Builder builder = DataSetDto.newBuilder();
            JsonFormat.parser().merge(json, builder);
            return builder.build();
        } catch (InvalidProtocolBufferException ex) {
            throw new RuntimeException(ex);
        }
    }
}
