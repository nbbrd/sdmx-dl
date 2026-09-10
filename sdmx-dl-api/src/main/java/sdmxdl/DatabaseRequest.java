package sdmxdl;

import lombok.NonNull;
import nbbrd.design.NonNegative;

@lombok.Value
@lombok.Builder
public class DatabaseRequest implements HasSearchQuery, HasLimit {

    public static final DatabaseRequest DEFAULT = DatabaseRequest.builder().build();

    @NonNull @lombok.Builder.Default
    DatabaseRef database = DatabaseRef.NO_DATABASE;

    @NonNull @lombok.Builder.Default
    Languages languages = Languages.ANY;

    @lombok.Builder.Default
    @NonNull String query = NO_QUERY;

    @lombok.Builder.Default
    @NonNegative int maxResults = NO_LIMIT;

    public static @NonNull Builder builderOf(@NonNull SourceRequest request) {
        return builder()
                .languages(request.getLanguages())
                .query(request.getQuery())
                .maxResults(request.getMaxResults());
    }

    public static final class Builder {

        public Builder databaseOf(@NonNull String database) {
            return database(DatabaseRef.parse(database));
        }

        public Builder languagesOf(@NonNull String languages) {
            return languages(Languages.parse(languages));
        }
    }
}
