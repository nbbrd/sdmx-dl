package sdmxdl;

import lombok.NonNull;

@lombok.Value
@lombok.Builder
public class DatabasesRequest implements HasSearch {

    public static final DatabasesRequest DEFAULT = DatabasesRequest.builder().build();

    @lombok.Builder.Default
    @NonNull Languages languages = Languages.ANY;

    @lombok.Builder.Default
    @NonNull String query = NO_QUERY;

    @lombok.Builder.Default
    int maxResults = AUTO_LIMIT;

    public static final class Builder {

        public Builder languagesOf(@NonNull String languages) {
            return languages(Languages.parse(languages));
        }
    }
}
