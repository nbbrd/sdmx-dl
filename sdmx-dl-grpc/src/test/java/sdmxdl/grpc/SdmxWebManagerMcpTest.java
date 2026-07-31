package sdmxdl.grpc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.quarkiverse.mcp.server.Content;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import sdmxdl.format.protobuf.CodelistDto;
import sdmxdl.format.protobuf.ConfidentialityDto;
import sdmxdl.format.protobuf.DataSetDto;
import sdmxdl.format.protobuf.MetaSetDto;
import sdmxdl.format.protobuf.ProtoApi;
import sdmxdl.format.protobuf.web.WebSourcesDto;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

@QuarkusTest
public class SdmxWebManagerMcpTest {

    // --- Moved from SdmxWebManagerServiceTest ---

    @Test
    public void mcpAbout() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpAbout", r -> {
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

    @Test
    public void mcpSearchFlowsReturnsRankedResults() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpSearchFlows", Map.of("source", "ECB", "query", "exchange rates"), r -> {
                    assertThat(r)
                            .returns(false, ToolResponse::isError)
                            .extracting(ToolResponse::content, list(Content.class))
                            .isNotEmpty();
                })
                .thenAssertResults();
    }

    @Test
    public void mcpSearchFlowsReturnsEmptyForEmptyQuery() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpSearchFlows", Map.of("source", "ECB", "query", ""), r -> {
                    assertThat(r)
                            .returns(false, ToolResponse::isError)
                            .extracting(ToolResponse::content, list(Content.class))
                            .hasSize(1)
                            .element(0)
                            .extracting(SdmxWebManagerMcpTest::getText, STRING)
                            .isEqualToIgnoringWhitespace("[]");
                })
                .thenAssertResults();
    }

    @Test
    public void mcpSearchFlowsReturnsErrorForInvalidSource() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpSearchFlows", Map.of("source", "INVALID_SOURCE_XYZ", "query", "test"), r -> {
                    assertThat(r)
                            .returns(true, ToolResponse::isError);
                })
                .thenAssertResults();
    }

    @Test
    public void mcpSearchSourcesReturnsRankedResults() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpSearchSources", Map.of("query", "european central"), r -> {
                    assertThat(r)
                            .returns(false, ToolResponse::isError)
                            .extracting(ToolResponse::content, list(Content.class))
                            .isNotEmpty();
                })
                .thenAssertResults();
    }

    @Test
    public void mcpSearchSourcesReturnsEmptyForEmptyQuery() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpSearchSources", Map.of("query", ""), r -> {
                    assertThat(r)
                            .returns(false, ToolResponse::isError)
                            .extracting(ToolResponse::content, list(Content.class))
                            .hasSize(1)
                            .element(0)
                            .extracting(SdmxWebManagerMcpTest::getText, STRING)
                            .isEqualToIgnoringWhitespace("[]");
                })
                .thenAssertResults();
    }

    @Test
    public void mcpSearchSourcesFindsById() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpSearchSources", Map.of("query", "ECB"), r -> {
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
    public void mcpSearchDatabasesReturnsEmptyForEmptyQuery() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpSearchDatabases", Map.of("source", "ECB", "query", ""), r -> {
                    assertThat(r)
                            .returns(false, ToolResponse::isError)
                            .extracting(ToolResponse::content, list(Content.class))
                            .hasSize(1)
                            .element(0)
                            .extracting(SdmxWebManagerMcpTest::getText, STRING)
                            .isEqualToIgnoringWhitespace("[]");
                })
                .thenAssertResults();
    }

    @Test
    public void mcpSearchDatabasesReturnsErrorForInvalidSource() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpSearchDatabases", Map.of("source", "INVALID_SOURCE_XYZ", "query", "test"), r -> {
                    assertThat(r)
                            .returns(true, ToolResponse::isError);
                })
                .thenAssertResults();
    }

    // --- Tier 1: compact sources + skeleton meta + codes ---

    @Test
    public void mcpSourcesReturnsCompactProjection() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpSources", r -> {
                    assertThat(r).returns(false, ToolResponse::isError);
                    WebSourcesDto sources = fromJson(WebSourcesDto.class, firstText(r));
                    assertThat(sources.getWebSourcesList())
                            .isNotEmpty()
                            .allSatisfy(source -> {
                                assertThat(source.getId()).isNotEmpty();
                                // compact projection drops endpoint/driver/properties/aliases/monitor
                                assertThat(source.getDriver()).isEmpty();
                                assertThat(source.getEndpoint()).isEmpty();
                                assertThat(source.getPropertiesMap()).isEmpty();
                                assertThat(source.getAliasesList()).isEmpty();
                                assertThat(source.getMonitor()).isEmpty();
                                // only public sources are exposed (default enum value is PUBLIC)
                                assertThat(source.getConfidentiality()).isEqualTo(ConfidentialityDto.PUBLIC);
                            });
                })
                .thenAssertResults();
    }

    @Test
    public void mcpMetaReturnsStructureSkeleton() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpMeta", Map.of("source", "ECB", "flow", "EXR"), r -> {
                    assertThat(r).returns(false, ToolResponse::isError);
                    MetaSetDto meta = fromJson(MetaSetDto.class, firstText(r));
                    assertThat(meta.getStructure().getDimensionsList())
                            .isNotEmpty()
                            .allSatisfy(dimension -> {
                                if (dimension.hasCodelist()) {
                                    // skeleton: codelist ref + count are kept, but codes are stripped
                                    assertThat(dimension.getCodelist().getRef()).isNotEmpty();
                                    assertThat(dimension.getCodelist().getCodeCount()).isPositive();
                                    assertThat(dimension.getCodelist().getCodesMap()).isEmpty();
                                }
                            });
                })
                .thenAssertResults();
    }

    @Test
    public void mcpCodesFiltersByQuery() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpCodes", Map.of("source", "ECB", "flow", "EXR", "dimension", "CURRENCY", "query", "CHF"), r -> {
                    assertThat(r).returns(false, ToolResponse::isError);
                    CodelistDto codes = fromJson(CodelistDto.class, firstText(r));
                    assertThat(codes.getCodesMap()).containsKey("CHF");
                    // total code count exceeds the filtered subset
                    assertThat(codes.getCodeCount()).isGreaterThan(codes.getCodesCount());
                })
                .thenAssertResults();
    }

    @Test
    public void mcpCodesIsCaseInsensitiveOnDimension() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpCodes", Map.of("source", "ECB", "flow", "EXR", "dimension", "currency", "query", "CHF"), r -> {
                    assertThat(r).returns(false, ToolResponse::isError);
                    CodelistDto codes = fromJson(CodelistDto.class, firstText(r));
                    assertThat(codes.getCodesMap()).containsKey("CHF");
                })
                .thenAssertResults();
    }

    @Test
    public void mcpCodesReturnsErrorForUnknownDimension() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpCodes", Map.of("source", "ECB", "flow", "EXR", "dimension", "NOT_A_DIMENSION"), r -> {
                    assertThat(r).returns(true, ToolResponse::isError);
                })
                .thenAssertResults();
    }

    // --- Tier 2: structured dimension filters + cheaper defaults ---

    @Test
    public void mcpDataAcceptsStructuredDimensions() {
        Map<String, String> dimensions = Map.of(
                "FREQ", "M",
                "CURRENCY", "CHF",
                "CURRENCY_DENOM", "EUR",
                "EXR_TYPE", "SP00",
                "EXR_SUFFIX", "A");
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpData", Map.of("source", "ECB", "flow", "EXR", "dimensions", dimensions), r -> {
                    assertThat(r).returns(false, ToolResponse::isError);
                    DataSetDto data = fromJson(DataSetDto.class, firstText(r));
                    assertThat(data.getDataList()).hasSize(1);
                    assertThat(data.getData(0).getKey()).isEqualTo("M.CHF.EUR.SP00.A");
                })
                .thenAssertResults();
    }

    // --- Tier 3: observation trimming metadata + instructive errors ---

    @Test
    public void mcpDataTrimsObservationsAndReportsTruncation() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpData", Map.of("source", "ECB", "flow", "EXR", "key", "M.CHF.EUR.SP00.A"), r -> {
                    assertThat(r).returns(false, ToolResponse::isError);
                    DataSetDto data = fromJson(DataSetDto.class, firstText(r));
                    assertThat(data.getDataList()).hasSize(1);
                    // default lastN caps observations
                    assertThat(data.getData(0).getObsCount()).isLessThanOrEqualTo(20);
                    // a long monthly series is truncated and carries metadata markers
                    assertThat(data.getData(0).getMetaMap())
                            .containsEntry("sdmxdl.obs.truncated", "true")
                            .containsKey("sdmxdl.obs.total")
                            .containsKey("sdmxdl.obs.returned");
                })
                .thenAssertResults();
    }

    @Test
    public void mcpDataUnknownSourceReturnsInstructiveError() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpData", Map.of("source", "INVALID_SOURCE_XYZ", "flow", "EXR"), r -> {
                    assertThat(r)
                            .returns(true, ToolResponse::isError)
                            .extracting(SdmxWebManagerMcpTest::firstText, STRING)
                            .contains("mcpSources");
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
            Message.Builder result = (Message.Builder) type.getMethod("newBuilder").invoke(null);
            JsonFormat.parser().merge(json, result);
            return type.cast(result.build());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | IOException e) {
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

