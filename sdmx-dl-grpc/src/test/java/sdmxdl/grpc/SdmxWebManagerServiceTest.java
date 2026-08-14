package sdmxdl.grpc;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import sdmxdl.format.protobuf.DatabaseDto;
import sdmxdl.format.protobuf.FlowDto;
import sdmxdl.format.protobuf.web.WebSourceDto;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class SdmxWebManagerServiceTest {

    @GrpcClient
    SdmxWebManager grpc;

    @Test
    public void testGetSources() {
        EmptyDto request = EmptyDto.newBuilder().build();
        List<WebSourceDto> response = grpc.getSources(request).collect().asList().await().atMost(Duration.ofSeconds(5));
        assertThat(response)
                .hasSizeGreaterThanOrEqualTo(33)
                .extracting(WebSourceDto::getId)
                .contains("ECB");
    }

    @Test
    public void testSearchFlowsReturnsRankedResults() {
        SearchFlowsRequestDto request = SearchFlowsRequestDto.newBuilder()
                .setSource("ECB")
                .setQuery("exchange rates")
                .setMaxResults(5)
                .build();
        List<FlowDto> response = grpc.searchFlows(request).collect().asList().await().atMost(Duration.ofSeconds(30));
        assertThat(response)
                .isNotEmpty()
                .hasSizeLessThanOrEqualTo(5);
        assertThat(response.get(0).getRef())
                .containsIgnoringCase("EXR");
    }

    @Test
    public void testSearchFlowsReturnsEmptyForEmptyQuery() {
        SearchFlowsRequestDto request = SearchFlowsRequestDto.newBuilder()
                .setSource("ECB")
                .setQuery("")
                .setMaxResults(10)
                .build();
        List<FlowDto> response = grpc.searchFlows(request).collect().asList().await().atMost(Duration.ofSeconds(30));
        assertThat(response).isEmpty();
    }

    @Test
    public void testSearchFlowsRespectsMaxResults() {
        SearchFlowsRequestDto request = SearchFlowsRequestDto.newBuilder()
                .setSource("ECB")
                .setQuery("balance")
                .setMaxResults(1)
                .build();
        List<FlowDto> response = grpc.searchFlows(request).collect().asList().await().atMost(Duration.ofSeconds(30));
        assertThat(response).hasSizeLessThanOrEqualTo(1);
    }

    @Test
    public void searchSourcesReturnsRankedResults() {
        SearchSourcesRequestDto request = SearchSourcesRequestDto.newBuilder()
                .setQuery("european central")
                .setMaxResults(5)
                .build();
        List<WebSourceDto> response = grpc.searchSources(request).collect().asList().await().atMost(Duration.ofSeconds(5));
        assertThat(response)
                .isNotEmpty()
                .hasSizeLessThanOrEqualTo(5);
        assertThat(response.get(0).getId())
                .isEqualTo("ECB");
    }

    @Test
    public void searchSourcesReturnsEmptyForEmptyQuery() {
        SearchSourcesRequestDto request = SearchSourcesRequestDto.newBuilder()
                .setQuery("")
                .setMaxResults(10)
                .build();
        List<WebSourceDto> response = grpc.searchSources(request).collect().asList().await().atMost(Duration.ofSeconds(5));
        assertThat(response).isEmpty();
    }

    @Test
    public void searchSourcesRespectsMaxResults() {
        SearchSourcesRequestDto request = SearchSourcesRequestDto.newBuilder()
                .setQuery("bank")
                .setMaxResults(2)
                .build();
        List<WebSourceDto> response = grpc.searchSources(request).collect().asList().await().atMost(Duration.ofSeconds(5));
        assertThat(response).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    public void searchSourcesFindsBySourceId() {
        SearchSourcesRequestDto request = SearchSourcesRequestDto.newBuilder()
                .setQuery("ECB")
                .setMaxResults(5)
                .build();
        List<WebSourceDto> response = grpc.searchSources(request).collect().asList().await().atMost(Duration.ofSeconds(5));
        assertThat(response)
                .isNotEmpty()
                .extracting(WebSourceDto::getId)
                .contains("ECB");
    }

    @Test
    public void searchDatabasesReturnsEmptyForEmptyQuery() {
        SearchDatabaseRequestDto request = SearchDatabaseRequestDto.newBuilder()
                .setSource("ECB")
                .setQuery("")
                .setMaxResults(10)
                .build();
        List<DatabaseDto> response = grpc.searchDatabases(request).collect().asList().await().atMost(Duration.ofSeconds(30));
        assertThat(response).isEmpty();
    }
}

