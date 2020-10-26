package sdmxdl;

import nbbrd.design.SealedType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

/**
 * Abstract component of a DataStructure.
 */
@SealedType({
        Dimension.class,
        Attribute.class
})
public abstract class Component {

    @NonNull
    public abstract String getId();

    /**
     * Non-null map of code description by code id that represents a codelist
     * (predefined sets of terms from which some statistical coded concepts take
     * their values).
     */
    @NonNull
    public abstract Map<String, String> getCodes();

    /**
     * Localized label for this concept.
     */
    @NonNull
    public abstract String getLabel();

    public static abstract class Builder<B extends Builder<B>> {

        @NonNull
        public abstract B id(@NonNull String id);

        @NonNull
        public abstract B code(@NonNull String key, @NonNull String value);

        @NonNull
        public abstract B codes(@NonNull Map<? extends String, ? extends String> codes);

        @NonNull
        public abstract B label(@NonNull String label);
    }
}
