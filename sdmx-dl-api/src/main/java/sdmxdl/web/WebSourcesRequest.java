package sdmxdl.web;

import lombok.Builder;
import lombok.NonNull;
import nbbrd.design.NonNegative;
import sdmxdl.Confidentiality;
import sdmxdl.HasLimit;
import sdmxdl.HasSearchQuery;
import sdmxdl.Languages;

@lombok.Value
@lombok.Builder
public class WebSourcesRequest implements HasSearchQuery, HasLimit {

    public static final WebSourcesRequest DEFAULT = WebSourcesRequest.builder().build();

    @lombok.Builder.Default
    @NonNull Languages languages = Languages.ANY;

    @lombok.Builder.Default
    @NonNull String query = NO_QUERY;

    @lombok.Builder.Default
    @NonNegative int maxResults = NO_LIMIT;

    @lombok.Builder.Default
    @NonNull Confidentiality threshold = Confidentiality.SECRET;

    public static final class Builder {

        public Builder languagesOf(@NonNull String languages) {
            return languages(Languages.parse(languages));
        }
    }
}
