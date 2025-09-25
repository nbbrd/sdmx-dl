package sdmxdl;

import lombok.NonNull;

@lombok.Value
@lombok.Builder
public class SourceRequest {

    @NonNull
    @lombok.Builder.Default
    Languages languages = Languages.ANY;

    public static final class Builder {

        public Builder languagesOf(@NonNull String languages) {
            return languages(Languages.parse(languages));
        }
    }
}
