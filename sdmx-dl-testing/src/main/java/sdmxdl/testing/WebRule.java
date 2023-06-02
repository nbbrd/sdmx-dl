package sdmxdl.testing;

import lombok.NonNull;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import sdmxdl.provider.Validator;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        singleton = true,
        batch = true
)
public interface WebRule {

    //    @ServiceId
    @NonNull String getId();

    @NonNull Validator<WebResponse> getValidator();

    static @NonNull WebRule of(@NonNull String id, @NonNull Validator<WebResponse> validator) {
        return new WebRule() {
            @Override
            public @NonNull String getId() {
                return id;
            }

            @Override
            public @NonNull Validator<WebResponse> getValidator() {
                return validator;
            }
        };
    }
}
