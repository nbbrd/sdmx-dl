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

    @NonNull String getName();

    @NonNull Validator<WebResponse> getValidator();

    static @NonNull WebRule of(@NonNull String name, @NonNull Validator<WebResponse> validator) {
        return new WebRule() {
            @Override
            public @NonNull String getName() {
                return name;
            }

            @Override
            public @NonNull Validator<WebResponse> getValidator() {
                return validator;
            }
        };
    }
}
