package sdmxdl.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.quarkiverse.mcp.server.Content;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import sdmxdl.format.protobuf.CodelistDto;
import sdmxdl.format.protobuf.ConfidentialityDto;
import sdmxdl.format.protobuf.DataSetDto;
import sdmxdl.format.protobuf.MetaSetDto;
import sdmxdl.format.protobuf.ProtoApi;
import sdmxdl.format.protobuf.web.WebSourceDto;

@QuarkusTest
public class SdmxWebManagerMcpTest {

    // --- about ---

    @Test
    public void about() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("about", r -> {
                    assertThat(r)
                            .returns(false, ToolResponse::isError)
                            .extracting(ToolResponse::content, list(Content.class))
                            .hasSize(1)
                            .element(0)
                            .extracting(SdmxWebManagerMcpTest::getText, STRING)
                            .isEqualToIgnoringNewLines(toJson(ProtoApi.fromAbout()));
                })
                .thenAssertResults();
    }

    // --- listFlows: merged list + search ---

    @Test
    public void flowsSearchReturnsRankedResults() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("listFlows", Map.of("source", "ECB", "query", "exchange rates"), r -> {
                    assertThat(r)
                            .returns(false, ToolResponse::isError)
                            .extracting(ToolResponse::content, list(Content.class))
                            .isNotEmpty();
                })
                .thenAssertResults();
    }

    @Test
    public void flowsReturnsAllSortedForEmptyQuery() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("listFlows", Map.of("source", "ECB", "query", ""), r -> {
                    assertThat(r)
                            .returns(false, ToolResponse::isError)
                            .extracting(ToolResponse::content, list(Content.class))
                            .hasSize(1)
                            .element(0)
                            .extracting(SdmxWebManagerMcpTest::getText, STRING)
                            .isNotEqualTo("[]");
                })
                .thenAssertResults();
    }

    @Test
    public void flowsMaxResultsTruncatesListing() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("listFlows", Map.of("source", "ECB", "maxResults", 2), r -> {
                    assertThat(r).returns(false, ToolResponse::isError);
                    List<JsonNode> flows = fromJsonArray(firstText(r));
                    assertThat(flows).hasSize(2);
                })
                .thenAssertResults();
    }

    @Test
    public void flowsReturnsErrorForInvalidSource() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("listFlows", Map.of("source", "INVALID_SOURCE_XYZ", "query", "test"), r -> {
                    assertThat(r).returns(true, ToolResponse::isError);
                })
                .thenAssertResults();
    }

    // --- listSources: merged list + search ---

    @Test
    public void sourcesSearchReturnsRankedResults() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("listSources", Map.of("query", "european central"), r -> {
                    assertThat(r)
                            .returns(false, ToolResponse::isError)
                            .extracting(ToolResponse::content, list(Content.class))
                            .isNotEmpty();
                })
                .thenAssertResults();
    }

    @Test
    public void sourcesReturnsAllForEmptyQuery() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("listSources", Map.of("query", ""), r -> {
                    assertThat(r).returns(false, ToolResponse::isError);
                    List<JsonNode> sources = fromJsonArray(firstText(r));
                    assertThat(sources).isNotEmpty();
                })
                .thenAssertResults();
    }

    @Test
    public void sourcesSearchFindsById() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("listSources", Map.of("query", "ECB"), r -> {
                    assertThat(r)
                            .returns(false, ToolResponse::isError)
                            .extracting(ToolResponse::content, list(Content.class))
                            .isNotEmpty()
                            .element(0)
                            .extracting(SdmxWebManagerMcpTest::getText, STRING)
                            .contains("ECB");
                })
                .thenAssertResults();
    }

    @Test
    public void sourcesReturnsCompactProjection() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("listSources", r -> {
                    assertThat(r).returns(false, ToolResponse::isError);
                    List<WebSourceDto> sources = fromJsonList(WebSourceDto.class, firstText(r));
                    assertThat(sources).isNotEmpty().allSatisfy(source -> {
                        assertThat(source.getId()).isNotEmpty();
                        // compact projection drops
                        // endpoint/driver/properties/aliases/monitor
                        assertThat(source.getDriver()).isEmpty();
                        assertThat(source.getEndpoint()).isEmpty();
                        assertThat(source.getPropertiesMap()).isEmpty();
                        assertThat(source.getAliasesList()).isEmpty();
                        assertThat(source.getMonitor()).isEmpty();
                        // only public sources are exposed (default enum
                        // value is PUBLIC)
                        assertThat(source.getConfidentiality()).isEqualTo(ConfidentialityDto.PUBLIC);
                    });
                })
                .thenAssertResults();
    }

    // --- listDatabases: merged list + search ---

    @Test
    public void databasesReturnsAllForEmptyQuery() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("listDatabases", Map.of("source", "ECB", "query", ""), r -> {
                    assertThat(r).returns(false, ToolResponse::isError);
                })
                .thenAssertResults();
    }

    @Test
    public void databasesReturnsErrorForInvalidSource() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("listDatabases", Map.of("source", "INVALID_SOURCE_XYZ", "query", "test"), r -> {
                    assertThat(r).returns(true, ToolResponse::isError);
                })
                .thenAssertResults();
    }

    // --- listDimensions / listAttributes: new merged list + search ---

    @Test
    public void dimensionsListsAllForEmptyQuery() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("listDimensions", Map.of("source", "ECB", "flow", "EXR"), r -> {
                    assertThat(r).returns(false, ToolResponse::isError);
                    List<JsonNode> dimensions = fromJsonArray(firstText(r));
                    assertThat(dimensions).isNotEmpty();
                    assertThat(dimensions.stream().map(node -> node.get("id").asText()))
                            .contains("FREQ");
                })
                .thenAssertResults();
    }

    @Test
    public void dimensionsSearchFiltersByQuery() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("listDimensions", Map.of("source", "ECB", "flow", "EXR", "query", "currency"), r -> {
                    assertThat(r)
                            .returns(false, ToolResponse::isError)
                            .extracting(ToolResponse::content, list(Content.class))
                            .isNotEmpty();
                })
                .thenAssertResults();
    }

    @Test
    public void attributesListsAllForEmptyQuery() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("listAttributes", Map.of("source", "ECB", "flow", "EXR"), r -> {
                    assertThat(r).returns(false, ToolResponse::isError);
                    List<JsonNode> attributes = fromJsonArray(firstText(r));
                    assertThat(attributes).isNotEmpty();
                })
                .thenAssertResults();
    }

    // --- getMeta + listCodes ---

    @Test
    public void metaReturnsStructureSkeleton() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("getMeta", Map.of("source", "ECB", "flow", "EXR"), r -> {
                    assertThat(r).returns(false, ToolResponse::isError);
                    MetaSetDto meta = fromJson(MetaSetDto.class, firstText(r));
                    assertThat(meta.getStructure().getDimensionsList())
                            .isNotEmpty()
                            .allSatisfy(dimension -> {
                                if (dimension.hasCodelist()) {
                                    // skeleton: codelist ref + count are kept, but
                                    // codes are stripped
                                    assertThat(dimension.getCodelist().getRef()).isNotEmpty();
                                    assertThat(dimension.getCodelist().getCodeCount())
                                            .isPositive();
                                    assertThat(dimension.getCodelist().getCodesMap())
                                            .isEmpty();
                                }
                            });
                })
                .thenAssertResults();
    }

    @Test
    public void codesFiltersByQuery() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall(
                        "listCodes",
                        Map.of("source", "ECB", "flow", "EXR", "dimension", "CURRENCY", "query", "CHF"),
                        r -> {
                            assertThat(r).returns(false, ToolResponse::isError);
                            CodelistDto codes = fromJson(CodelistDto.class, firstText(r));
                            assertThat(codes.getCodesMap()).containsKey("CHF");
                            // total code count exceeds the filtered subset
                            assertThat(codes.getCodeCount()).isGreaterThanOrEqualTo(codes.getCodesCount());
                        })
                .thenAssertResults();
    }

    @Test
    public void codesReturnsErrorForUnknownDimension() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("listCodes", Map.of("source", "ECB", "flow", "EXR", "dimension", "NOT_A_DIMENSION"), r -> {
                    assertThat(r).returns(true, ToolResponse::isError);
                })
                .thenAssertResults();
    }

    // --- getData: structured dimension filters + observation filtering ---

    @Test
    public void dataAcceptsStructuredDimensions() {
        Map<String, String> dimensions = Map.of(
                "FREQ", "M",
                "CURRENCY", "CHF",
                "CURRENCY_DENOM", "EUR",
                "EXR_TYPE", "SP00",
                "EXR_SUFFIX", "A");
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("getData", Map.of("source", "ECB", "flow", "EXR", "dimensions", dimensions), r -> {
                    assertThat(r).returns(false, ToolResponse::isError);
                    DataSetDto data = fromJson(DataSetDto.class, firstText(r));
                    assertThat(data.getDataList()).hasSize(1);
                    assertThat(data.getData(0).getKey()).isEqualTo("M.CHF.EUR.SP00.A");
                })
                .thenAssertResults();
    }

    @Test
    public void dataCapsObservationsWithDefaultLastN() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("getData", Map.of("source", "ECB", "flow", "EXR", "key", "M.CHF.EUR.SP00.A"), r -> {
                    assertThat(r).returns(false, ToolResponse::isError);
                    DataSetDto data = fromJson(DataSetDto.class, firstText(r));
                    assertThat(data.getDataList()).hasSize(1);
                    // default lastN caps observations
                    assertThat(data.getData(0).getObsCount()).isLessThanOrEqualTo(20);
                    // the returned query echoes the applied filter
                    assertThat(data.getQuery().getLastNObservations()).isEqualTo(20);
                })
                .thenAssertResults();
    }

    @Test
    public void dataSupportsFirstNObservations() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall(
                        "getData",
                        Map.of("source", "ECB", "flow", "EXR", "key", "M.CHF.EUR.SP00.A", "firstN", "1", "lastN", "0"),
                        r -> {
                            assertThat(r).returns(false, ToolResponse::isError);
                            DataSetDto data = fromJson(DataSetDto.class, firstText(r));
                            assertThat(data.getDataList()).hasSize(1);
                            assertThat(data.getData(0).getObsCount()).isEqualTo(1);
                            assertThat(data.getQuery().getFirstNObservations()).isEqualTo(1);
                        })
                .thenAssertResults();
    }

    @Test
    public void dataSupportsPeriodRange() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall(
                        "getData",
                        Map.of(
                                "source",
                                "ECB",
                                "flow",
                                "EXR",
                                "key",
                                "M.CHF.EUR.SP00.A",
                                "startPeriod",
                                "2020-01",
                                "endPeriod",
                                "2020-12",
                                "lastN",
                                "0"),
                        r -> {
                            assertThat(r).returns(false, ToolResponse::isError);
                            DataSetDto data = fromJson(DataSetDto.class, firstText(r));
                            assertThat(data.getDataList()).hasSize(1);
                            // a one-year monthly window holds at most 12 observations
                            assertThat(data.getData(0).getObsCount()).isBetween(1, 12);
                            // the returned query echoes the applied bounds
                            assertThat(data.getQuery().getStartPeriod()).startsWith("2020-01");
                            assertThat(data.getQuery().getEndPeriod()).startsWith("2020-12");
                        })
                .thenAssertResults();
    }

    @Test
    public void dataUnknownSourceReturnsInstructiveError() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("getData", Map.of("source", "INVALID_SOURCE_XYZ", "flow", "EXR"), r -> {
                    assertThat(r)
                            .returns(true, ToolResponse::isError)
                            .extracting(SdmxWebManagerMcpTest::firstText, STRING)
                            .contains("listSources");
                })
                .thenAssertResults();
    }

    // --- Helpers ---

    private static String toJson(Message message) {
        try {
            return JsonFormat.printer().print(message);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T extends Message> T fromJson(Class<T> type, String json) {
        try {
            Message.Builder result =
                    (Message.Builder) type.getMethod("newBuilder").invoke(null);
            JsonFormat.parser().merge(json, result);
            return type.cast(result.build());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T extends Message> List<T> fromJsonList(Class<T> type, String jsonArray) {
        List<T> result = new ArrayList<>();
        for (JsonNode node : fromJsonArray(jsonArray)) {
            result.add(fromJson(type, node.toString()));
        }
        return result;
    }

    private static List<JsonNode> fromJsonArray(String jsonArray) {
        try {
            List<JsonNode> result = new ArrayList<>();
            new ObjectMapper().readTree(jsonArray).forEach(result::add);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getText(Content content) {
        return content.asText().text();
    }

    private static String firstText(ToolResponse response) {
        return getText(response.content().get(0));
    }
}
