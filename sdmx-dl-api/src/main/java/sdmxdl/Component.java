package sdmxdl;

import lombok.NonNull;
import nbbrd.design.SealedType;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * Abstract component of a DataStructure.
 */
@SealedType({
        Dimension.class,
        Attribute.class
})
public abstract class Component implements HasName {

    public abstract @NonNull String getId();

    public abstract @Nullable Codelist getCodelist();

    /**
     * Check if this component has a codelist.
     *
     * @return true if codelist, false otherwise
     */
    public final boolean isCoded() {
        return getCodelist() != null;
    }

    public final @NonNull Map<String, String> getCodes() {
        Codelist codelist = getCodelist();
        return codelist != null ? codelist.getCodes() : Collections.emptyMap();
    }

    public static abstract class Builder<B extends Builder<B>> {

        @NonNull
        public abstract B id(@NonNull String id);

        public abstract @NonNull B codelist(@NonNull Codelist codelist);

        @NonNull
        public abstract B name(@NonNull String name);
    }
}
