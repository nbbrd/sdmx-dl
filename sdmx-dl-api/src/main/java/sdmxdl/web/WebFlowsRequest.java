package sdmxdl.web;

import java.time.Duration;
import java.util.List;
import lombok.NonNull;
import nbbrd.design.NonNegative;
import org.jspecify.annotations.Nullable;
import sdmxdl.Confidentiality;
import sdmxdl.HasDescription;
import sdmxdl.HasSearch;
import sdmxdl.Languages;

/**
 * Request to search the flows of several web sources at once.
 *
 * @see SdmxWebManager#searchFlows(WebFlowsRequest)
 */
@lombok.Value
@lombok.Builder
public class WebFlowsRequest implements HasSearch {

    public static final WebFlowsRequest DEFAULT = WebFlowsRequest.builder().build();

    /**
     * Ids of the sources to search; empty means every non-alias source.
     */
    @lombok.Singular
    @NonNull List<String> sources;

    @lombok.Builder.Default
    @NonNull Confidentiality confidentialityThreshold = Confidentiality.SECRET;

    @lombok.Builder.Default
    @NonNull Languages languages = Languages.ANY;

    @lombok.Builder.Default
    @NonNull String query = NO_QUERY;

    @lombok.Builder.Default
    int maxResults = AUTO_LIMIT;

    @lombok.Builder.Default
    int offset = NO_OFFSET;

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

    /**
     * Maximum time to wait for the flows of the sources, or {@code null} to wait indefinitely.
     * Sources that do not answer in time are reported as failures.
     */
    @Nullable Duration timeout;

    public static final class Builder {

        public Builder languagesOf(@NonNull String languages) {
            return languages(Languages.parse(languages));
        }
    }
}
