package sdmxdl;

import lombok.NonNull;
import nbbrd.design.NonNegative;

@lombok.Value
@lombok.Builder
public class FlowsRequest implements HasSearch, Request {

    public static final FlowsRequest DEFAULT = FlowsRequest.builder().build();

    @NonNull @lombok.Builder.Default
    DatabaseRef database = DatabaseRef.NO_DATABASE;

    @NonNull @lombok.Builder.Default
    Languages languages = Languages.ANY;

    @lombok.Builder.Default
    @NonNull String query = NO_QUERY;

    @lombok.Builder.Default
    int maxResults = AUTO_LIMIT;

    /**
     * Whether flow descriptions should have markup (e.g. HTML tags) stripped
     * and whitespace collapsed before being returned.
     */
    @lombok.Builder.Default
    boolean plainText = false;

    /**
     * Maximum length of flow descriptions (truncated with an ellipsis),
     * or {@link HasDescription#NO_DESCRIPTION_LIMIT} for no truncation.
     */
    @lombok.Builder.Default
    @NonNegative int truncate = HasDescription.NO_DESCRIPTION_LIMIT;

    @Override
    public <T> T accept(@NonNull RequestVisitor<T> visitor) {
        return visitor.visitFlows(this);
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
