package sdmxdl.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.grpc.ProtoGrpc.*;

import org.junit.jupiter.api.Test;
import sdmxdl.*;

class ProtoGrpcTest {

    @Test
    void testSourceRequest() {
        DatabasesRequest empty = DatabasesRequest.DEFAULT;
        assertThat(toSourceRequest(fromSourceRequest("src", empty))).isEqualTo(empty);

        DatabasesRequest request = DatabasesRequest.builder().languagesOf("en").build();
        assertThat(toSourceRequest(fromSourceRequest("src", request))).isEqualTo(request);
    }

    @Test
    void testDatabaseRequest() {
        FlowsRequest empty = FlowsRequest.DEFAULT;
        assertThat(toDatabaseRequest(fromDatabaseRequest("src", empty))).isEqualTo(empty);

        FlowsRequest request =
                FlowsRequest.builder().databaseOf("hello").languagesOf("en").build();
        assertThat(toDatabaseRequest(fromDatabaseRequest("src", request))).isEqualTo(request);
    }

    @Test
    void testFlowRequest() {
        MetaRequest empty = MetaRequest.builder().flowOf("EXR").build();
        assertThat(toFlowRequest(fromFlowRequest("src", empty))).isEqualTo(empty);

        MetaRequest request = MetaRequest.builder()
                .flowOf("EXR")
                .databaseOf("hello")
                .languagesOf("en")
                .build();
        assertThat(toFlowRequest(fromFlowRequest("src", request))).isEqualTo(request);
    }

    @Test
    void testKeyRequest() {
        DataRequest empty = DataRequest.builder().flowOf("EXR").build();
        assertThat(toKeyRequest(fromKeyRequest("src", empty))).isEqualTo(empty);

        DataRequest request = DataRequest.builder()
                .flowOf("EXR")
                .keyOf("A.B.C")
                .databaseOf("hello")
                .languagesOf("en")
                .build();
        assertThat(toKeyRequest(fromKeyRequest("src", request))).isEqualTo(request);
    }
}
