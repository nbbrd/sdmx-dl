package sdmxdl.testing;

import lombok.NonNull;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceId;
import sdmxdl.provider.Validator;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        singleton = true,
        batchType = WebRuleBatch.class
)
public interface WebRule {

    @ServiceId(pattern = ServiceId.SCREAMING_SNAKE_CASE)
    @NonNull String getId();

    @NonNull Validator<WebResponse> getValidator();
}
