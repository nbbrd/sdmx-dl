package sdmxdl;

import lombok.NonNull;

@lombok.Value
@lombok.Builder
public class AvailabilityRequest {

    @lombok.Builder.Default
    @NonNull DatabaseRef database = DatabaseRef.NO_DATABASE;

    @NonNull FlowRef flow;

    @NonNull Key key;

    @NonNull String dimension;

    @lombok.Builder.Default
    @NonNull Languages languages = Languages.ANY;

    public static final class Builder {

        public Builder databaseOf(@NonNull String database) {
            return database(DatabaseRef.parse(database));
        }

        public Builder flowOf(@NonNull String flow) {
            return flow(FlowRef.parse(flow));
        }

        public Builder keyOf(@NonNull String key) {
            return key(Key.parse(key));
        }

        public Builder languagesOf(@NonNull String languages) {
            return languages(Languages.parse(languages));
        }
    }
}
