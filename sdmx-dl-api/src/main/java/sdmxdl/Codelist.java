package sdmxdl;

import java.util.Map;

@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.EqualsAndHashCode(callSuper = false)
public class Codelist extends Resource<CodelistRef> {

    @lombok.NonNull
    CodelistRef ref;

    /**
     * Non-null map of code description by code id that represents a codelist
     * (predefined sets of terms from which some statistical coded concepts take
     * their values).
     */
    @lombok.NonNull
    @lombok.Singular
    Map<String, String> codes;
}
