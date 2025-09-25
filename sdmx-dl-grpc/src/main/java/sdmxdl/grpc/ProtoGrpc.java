package sdmxdl.grpc;

import sdmxdl.DatabaseRef;
import sdmxdl.Languages;

@lombok.experimental.UtilityClass
public class ProtoGrpc {

    public static SourceRequest fromSourceRequest(String source, sdmxdl.SourceRequest value) {
        SourceRequest.Builder result = SourceRequest.newBuilder();
        result.setSource(source);
        result.setLanguages(value.getLanguages().toString());
        return result.build();
    }

    public static sdmxdl.SourceRequest toSourceRequest(SourceRequest value) {
        return sdmxdl.SourceRequest
                .builder()
                .languagesOf(value.hasLanguages() ? value.getLanguages() : "")
                .build();
    }

    public static DatabaseRequest fromDatabaseRequest(String source, sdmxdl.DatabaseRequest value) {
        DatabaseRequest.Builder result = DatabaseRequest.newBuilder();
        result.setSource(source);
        result.setDatabase(value.getDatabase().toString());
        result.setLanguages(value.getLanguages().toString());
        return result.build();
    }

    public static sdmxdl.DatabaseRequest toDatabaseRequest(DatabaseRequest value) {
        return sdmxdl.DatabaseRequest
                .builder()
                .databaseOf(value.hasDatabase() ? value.getDatabase() : "")
                .languagesOf(value.hasLanguages() ? value.getLanguages() : "")
                .build();
    }

    public static FlowRequest fromFlowRequest(String source, sdmxdl.FlowRequest value) {
        FlowRequest.Builder result = FlowRequest.newBuilder();
        result.setSource(source);
        result.setDatabase(value.getDatabase().toString());
        result.setFlow(value.getFlow().toString());
        result.setLanguages(value.getLanguages().toString());
        return result.build();
    }

    public static sdmxdl.FlowRequest toFlowRequest(FlowRequest value) {
        return sdmxdl.FlowRequest
                .builder()
                .databaseOf(value.hasDatabase() ? value.getDatabase() : "")
                .flowOf(value.getFlow())
                .languagesOf(value.hasLanguages() ? value.getLanguages() : "")
                .build();
    }

    public static KeyRequest fromKeyRequest(String source, sdmxdl.KeyRequest value) {
        KeyRequest.Builder result = KeyRequest.newBuilder();
        result.setSource(source);
        result.setDatabase(value.getDatabase().toString());
        result.setFlow(value.getFlow().toString());
        result.setKey(value.getKey().toString());
        result.setLanguages(value.getLanguages().toString());
        return result.build();
    }

    public static sdmxdl.KeyRequest toKeyRequest(KeyRequest value) {
        return sdmxdl.KeyRequest
                .builder()
                .databaseOf(value.hasDatabase() ? value.getDatabase() : DatabaseRef.NO_DATABASE_KEYWORD)
                .flowOf(value.getFlow())
                .keyOf(value.getKey())
                .languagesOf(value.hasLanguages() ? value.getLanguages() : Languages.ANY_KEYWORD)
                .build();
    }
}
