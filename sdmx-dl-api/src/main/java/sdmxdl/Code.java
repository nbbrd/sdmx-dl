package sdmxdl;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A code of a codelist, as returned by {@link Provider#listCodes(CodesRequest)}.
 */
@lombok.Value(staticConstructor = "of")
public class Code {

    /**
     * Id of the code.
     */
    @NonNull String id;

    /**
     * Label of the code, or {@code null} when unknown.
     */
    @Nullable String name;
}
