package sdmxdl.grpc;

import static sdmxdl.DatabaseRef.NO_DATABASE_KEYWORD;
import static sdmxdl.Languages.ANY_KEYWORD;

import java.time.LocalDateTime;
import sdmxdl.*;

@lombok.experimental.UtilityClass
public class ProtoGrpc {

    public static SourceRequestDto fromSourceRequest(String source, SourceRequest value) {
        SourceRequestDto.Builder result = SourceRequestDto.newBuilder();
        result.setSource(source);
        result.setLanguages(value.getLanguages().toString());
        return result.build();
    }

    public static SourceRequest toSourceRequest(SourceRequestDto value) {
        return SourceRequest.builder()
                .languagesOf(value.hasLanguages() ? value.getLanguages() : ANY_KEYWORD)
                .build();
    }

    public static DatabaseRequestDto fromDatabaseRequest(String source, DatabaseRequest value) {
        DatabaseRequestDto.Builder result = DatabaseRequestDto.newBuilder();
        result.setSource(source);
        result.setDatabase(value.getDatabase().toString());
        result.setLanguages(value.getLanguages().toString());
        return result.build();
    }

    public static DatabaseRequest toDatabaseRequest(DatabaseRequestDto value) {
        return DatabaseRequest.builder()
                .databaseOf(value.hasDatabase() ? value.getDatabase() : NO_DATABASE_KEYWORD)
                .languagesOf(value.hasLanguages() ? value.getLanguages() : ANY_KEYWORD)
                .build();
    }

    public static FlowRequestDto fromFlowRequest(String source, FlowRequest value) {
        FlowRequestDto.Builder result = FlowRequestDto.newBuilder();
        result.setSource(source);
        result.setDatabase(value.getDatabase().toString());
        result.setFlow(value.getFlow().toString());
        result.setLanguages(value.getLanguages().toString());
        return result.build();
    }

    public static FlowRequest toFlowRequest(FlowRequestDto value) {
        return FlowRequest.builder()
                .databaseOf(value.hasDatabase() ? value.getDatabase() : NO_DATABASE_KEYWORD)
                .flowOf(value.getFlow())
                .languagesOf(value.hasLanguages() ? value.getLanguages() : ANY_KEYWORD)
                .build();
    }

    public static KeyRequestDto fromKeyRequest(String source, KeyRequest value) {
        KeyRequestDto.Builder result = KeyRequestDto.newBuilder();
        result.setSource(source);
        result.setDatabase(value.getDatabase().toString());
        result.setFlow(value.getFlow().toString());
        result.setKey(value.getKey().toString());
        result.setLanguages(value.getLanguages().toString());
        if (value.getStartPeriod() != null) {
            result.setStartPeriod(value.getStartPeriod().toString());
        }
        if (value.getEndPeriod() != null) {
            result.setEndPeriod(value.getEndPeriod().toString());
        }
        if (value.getFirstNObservations() != null) {
            result.setFirstNObservations(value.getFirstNObservations());
        }
        if (value.getLastNObservations() != null) {
            result.setLastNObservations(value.getLastNObservations());
        }
        return result.build();
    }

    public static KeyRequest toKeyRequest(KeyRequestDto value) {
        KeyRequest.Builder result =
                KeyRequest.builder()
                        .databaseOf(value.hasDatabase() ? value.getDatabase() : NO_DATABASE_KEYWORD)
                        .flowOf(value.getFlow())
                        .keyOf(value.getKey())
                        .languagesOf(value.hasLanguages() ? value.getLanguages() : ANY_KEYWORD);
        if (value.hasStartPeriod()) {
            result.startPeriod(LocalDateTime.parse(value.getStartPeriod()));
        }
        if (value.hasEndPeriod()) {
            result.endPeriod(LocalDateTime.parse(value.getEndPeriod()));
        }
        if (value.hasFirstNObservations()) {
            result.firstNObservations(value.getFirstNObservations());
        }
        if (value.hasLastNObservations()) {
            result.lastNObservations(value.getLastNObservations());
        }
        return result.build();
    }
}
