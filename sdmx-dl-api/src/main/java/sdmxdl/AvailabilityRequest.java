package sdmxdl;

import lombok.NonNull;

/**
 * Parameters of {@link Provider#listAvailability(AvailabilityRequest)}.
 *
 * <p>The {@link #getDimension() dimension} is resolved against the structure of the flow as follows:
 * <ul>
 *     <li>an exact dimension id (e.g. {@code CURRENCY}) selects that dimension,</li>
 *     <li>otherwise, an integer (e.g. {@code 1}) selects the dimension at that zero-based index,</li>
 *     <li>an empty value ({@link #FIRST_WILDCARD_DIMENSION}, the default) selects the first wildcard
 *     dimension of the {@link #getKey() key}.</li>
 * </ul>
 */
@lombok.Value
@lombok.Builder
public class AvailabilityRequest implements Request {

    /**
     * Dimension value that selects the first wildcard dimension of the key.
     */
    public static final String FIRST_WILDCARD_DIMENSION = "";

    @lombok.Builder.Default
    @NonNull DatabaseRef database = DatabaseRef.NO_DATABASE;

    @NonNull FlowRef flow;

    @NonNull Key key;

    @lombok.Builder.Default
    @NonNull String dimension = FIRST_WILDCARD_DIMENSION;

    @lombok.Builder.Default
    @NonNull Languages languages = Languages.ANY;

    @Override
    public <T> T accept(@NonNull RequestVisitor<T> visitor) {
        return visitor.visitAvailability(this);
    }

    public static final class Builder {

        public Builder databaseOf(@NonNull String database) {
            return database(DatabaseRef.parse(database));
        }

        public Builder flowOf(@NonNull String flow) {
            return flow(FlowRef.parse(flow));
        }

        public Builder keyOf(@NonNull String key) {
            return key(Key.parse(key));
        }

        public Builder languagesOf(@NonNull String languages) {
            return languages(Languages.parse(languages));
        }
    }
}
