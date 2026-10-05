package sdmxdl;

import java.util.SortedMap;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Result of {@link Provider#listAvailability(AvailabilityRequest)}: the codes that actually
 * occur for a dimension under a key constraint.
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class Availability {

    /**
     * Id of the dimension the codes belong to, as resolved from {@link AvailabilityRequest#getDimension()}.
     */
    @NonNull String dimension;

    /**
     * Available codes sorted by id and mapped to their label, or to {@code null} when the label is unknown.
     */
    @lombok.Singular
    @NonNull SortedMap<String, @Nullable String> codes;
}
