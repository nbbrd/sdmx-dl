package sdmxdl;

import lombok.NonNull;

@lombok.Value
@lombok.Builder
public class DimensionsRequest implements HasSearch {

    @lombok.Builder.Default
    @NonNull DatabaseRef database = DatabaseRef.NO_DATABASE;

    @NonNull FlowRef flow;

    @lombok.Builder.Default
    @NonNull Languages languages = Languages.ANY;

    @lombok.Builder.Default
    @NonNull String query = NO_QUERY;

    @lombok.Builder.Default
    int maxResults = AUTO_LIMIT;

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
