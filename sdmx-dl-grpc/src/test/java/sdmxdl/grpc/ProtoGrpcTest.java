package sdmxdl.grpc;

import org.junit.jupiter.api.Test;
import sdmxdl.DatabaseRequest;
import sdmxdl.FlowRequest;
import sdmxdl.KeyRequest;
import sdmxdl.SourceRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.grpc.ProtoGrpc.*;

class ProtoGrpcTest {

    @Test
    void testSourceRequest() {
        SourceRequest empty = SourceRequest.builder().build();
        assertThat(toSourceRequest(fromSourceRequest("src", empty))).isEqualTo(empty);

        SourceRequest request = SourceRequest.builder().languagesOf("en").build();
        assertThat(toSourceRequest(fromSourceRequest("src", request))).isEqualTo(request);
    }

    @Test
    void testDatabaseRequest() {
        DatabaseRequest empty = DatabaseRequest.builder().build();
        assertThat(toDatabaseRequest(fromDatabaseRequest("src", empty))).isEqualTo(empty);

        DatabaseRequest request = DatabaseRequest.builder().databaseOf("hello").languagesOf("en").build();
        assertThat(toDatabaseRequest(fromDatabaseRequest("src", request))).isEqualTo(request);
    }

    @Test
    void testFlowRequest() {
        FlowRequest empty = FlowRequest.builder().flowOf("EXR").build();
        assertThat(toFlowRequest(fromFlowRequest("src", empty))).isEqualTo(empty);

        FlowRequest request = FlowRequest.builder().flowOf("EXR").databaseOf("hello").languagesOf("en").build();
        assertThat(toFlowRequest(fromFlowRequest("src", request))).isEqualTo(request);
    }

    @Test
    void testKeyRequest() {
        KeyRequest empty = KeyRequest.builder().flowOf("EXR").build();
        assertThat(toKeyRequest(fromKeyRequest("src", empty))).isEqualTo(empty);

        KeyRequest request = KeyRequest.builder().flowOf("EXR").keyOf("A.B.C").databaseOf("hello").languagesOf("en").build();
        assertThat(toKeyRequest(fromKeyRequest("src", request))).isEqualTo(request);
    }
}