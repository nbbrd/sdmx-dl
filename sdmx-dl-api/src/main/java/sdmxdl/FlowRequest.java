package sdmxdl;

import lombok.NonNull;

@lombok.Value
@lombok.Builder
public class FlowRequest {

    @NonNull
    @lombok.Builder.Default
    DatabaseRef database = DatabaseRef.NO_DATABASE;

    @NonNull
    FlowRef flow;

    @NonNull
    @lombok.Builder.Default
    Languages languages = Languages.ANY;

    public static @NonNull Builder builderOf(@NonNull DatabaseRequest request) {
        return builder()
                .database(request.getDatabase())
                .languages(request.getLanguages());
    }

    public static final class Builder {

        public Builder databaseOf(@NonNull String database) {
            return database(DatabaseRef.parse(database));
        }

        public Builder flowOf(@NonNull String flow) {
            return flow(FlowRef.parse(flow));
        }

        public Builder languagesOf(@NonNull String languages) {
            return languages(Languages.parse(languages));
        }
    }
}
