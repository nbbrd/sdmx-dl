package sdmxdl.testing;

import lombok.NonNull;
import sdmxdl.provider.Validator;

@lombok.Builder
public class WebRuleSupport implements WebRule {

    private final @NonNull String id;

    private final @NonNull Validator<WebResponse> validator;

    @Override
    public @NonNull String getId() {
        return id;
    }

    @Override
    public @NonNull Validator<WebResponse> getValidator() {
        return validator;
    }
}
