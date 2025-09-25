package sdmxdl.grpc;

@lombok.experimental.UtilityClass
public class ProtoGrpc {

    public static SourceRequest fromSourceRequest(sdmxdl.web.SourceRequest value) {
        SourceRequest.Builder result = SourceRequest.newBuilder();
        result.setSource(value.getSource());
        result.setLanguages(value.getLanguages().toString());
        return result.build();
    }

    public static sdmxdl.web.SourceRequest toSourceRequest(SourceRequest value) {
        return sdmxdl.web.SourceRequest
                .builder()
                .source(value.getSource())
                .languagesOf(value.hasLanguages() ? value.getLanguages() : "")
                .build();
    }

    public static DatabaseRequest fromDatabaseRequest(sdmxdl.web.DatabaseRequest value) {
        DatabaseRequest.Builder result = DatabaseRequest.newBuilder();
        result.setSource(value.getSource());
        result.setDatabase(value.getDatabase().toString());
        result.setLanguages(value.getLanguages().toString());
        return result.build();
    }

    public static sdmxdl.web.DatabaseRequest toDatabaseRequest(DatabaseRequest value) {
        return sdmxdl.web.DatabaseRequest
                .builder()
                .source(value.getSource())
                .databaseOf(value.hasDatabase() ? value.getDatabase() : "")
                .languagesOf(value.hasLanguages() ? value.getLanguages() : "")
                .build();
    }

    public static FlowRequest fromFlowRequest(sdmxdl.web.FlowRequest value) {
        FlowRequest.Builder result = FlowRequest.newBuilder();
        result.setSource(value.getSource());
        result.setDatabase(value.getDatabase().toString());
        result.setFlow(value.getFlow().toString());
        result.setLanguages(value.getLanguages().toString());
        return result.build();
    }

    public static sdmxdl.web.FlowRequest toFlowRequest(FlowRequest value) {
        return sdmxdl.web.FlowRequest
                .builder()
                .source(value.getSource())
                .databaseOf(value.hasDatabase() ? value.getDatabase() : "")
                .flowOf(value.getFlow())
                .languagesOf(value.hasLanguages() ? value.getLanguages() : "")
                .build();
    }

    public static KeyRequest fromKeyRequest(sdmxdl.web.KeyRequest value) {
        KeyRequest.Builder result = KeyRequest.newBuilder();
        result.setSource(value.getSource());
        result.setDatabase(value.getDatabase().toString());
        result.setFlow(value.getFlow().toString());
        result.setKey(value.getKey().toString());
        result.setLanguages(value.getLanguages().toString());
        return result.build();
    }

    public static sdmxdl.web.KeyRequest toKeyRequest(KeyRequest value) {
        return sdmxdl.web.KeyRequest
                .builder()
                .source(value.getSource())
                .databaseOf(value.hasDatabase() ? value.getDatabase() : "")
                .flowOf(value.getFlow())
                .keyOf(value.getKey())
                .languagesOf(value.hasLanguages() ? value.getLanguages() : "")
                .build();
    }
}
