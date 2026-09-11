package sdmxdl;

import lombok.NonNull;
import nbbrd.design.NonNegative;

@lombok.Value
@lombok.Builder
public class FlowsRequest implements HasSearchQuery, HasLimit {

    public static final FlowsRequest DEFAULT = FlowsRequest.builder().build();

    @NonNull @lombok.Builder.Default
    DatabaseRef database = DatabaseRef.NO_DATABASE;

    @NonNull @lombok.Builder.Default
    Languages languages = Languages.ANY;

    @lombok.Builder.Default
    @NonNull String query = NO_QUERY;

    @lombok.Builder.Default
    @NonNegative int maxResults = NO_LIMIT;

    public static final class Builder {

        public Builder databaseOf(@NonNull String database) {
            return database(DatabaseRef.parse(database));
        }

        public Builder languagesOf(@NonNull String languages) {
            return languages(Languages.parse(languages));
        }
    }
}
