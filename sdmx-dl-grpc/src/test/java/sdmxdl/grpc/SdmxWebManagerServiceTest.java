package sdmxdl.grpc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.quarkiverse.mcp.server.Content;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import sdmxdl.Confidentiality;
import sdmxdl.format.protobuf.ProtoApi;
import sdmxdl.format.protobuf.ProtoWeb;
import sdmxdl.format.protobuf.web.WebSource;
import sdmxdl.format.protobuf.web.WebSources;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

@QuarkusTest
public class SdmxWebManagerServiceTest {

    @GrpcClient
    SdmxWebManager grpc;

    @Test
    public void testGetSources() {
        Empty request = Empty.newBuilder().build();
        List<WebSource> response = grpc.getSources(request).collect().asList().await().atMost(Duration.ofSeconds(5));
        assertThat(response)
                .hasSizeGreaterThanOrEqualTo(34)
                .extracting(WebSource::getId)
                .contains("ECB");
    }

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
                            .extracting(SdmxWebManagerServiceTest::getText, STRING)
                            .isEqualToIgnoringNewLines(toJson(ProtoApi.fromAbout()));
                })
                .thenAssertResults();
    }

    @Test
    public void mcpSources() {
        McpAssured.newConnectedStreamableClient()
                .when()
                .toolsCall("mcpSources", r -> {
                    assertThat(r)
                            .returns(false, ToolResponse::isError)
                            .extracting(ToolResponse::content, list(Content.class))
                            .hasSize(1)
                            .element(0)
                            .extracting(content -> fromJson(WebSources.class, getText(content)))
                            .extracting(ProtoWeb::toWebSources)
                            .extracting(sdmxdl.web.WebSources::getSources, list(sdmxdl.web.WebSource.class))
                            .allMatch(source -> source.getConfidentiality().equals(Confidentiality.PUBLIC));
                })
                .thenAssertResults();
    }

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
}
