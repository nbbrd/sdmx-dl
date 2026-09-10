package sdmxdl;

import lombok.NonNull;
import nbbrd.design.NonNegative;

@lombok.Value
@lombok.Builder
public class SourceRequest implements HasSearchQuery, HasLimit {

    public static final SourceRequest DEFAULT = SourceRequest.builder().build();

    @lombok.Builder.Default
    @NonNull Languages languages = Languages.ANY;

    @lombok.Builder.Default
    @NonNull String query = NO_QUERY;

    @lombok.Builder.Default
    @NonNegative int maxResults = NO_LIMIT;

    public static final class Builder {

        public Builder languagesOf(@NonNull String languages) {
            return languages(Languages.parse(languages));
        }
    }
}
