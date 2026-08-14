package internal.sdmxdl.format.xml;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.CodelistRef;

import java.util.Optional;

// TODO: move to API?
@lombok.Value
class Concept {

    @NonNull
    String id;

    @NonNull
    String name;

    @Nullable
    CodelistRef coreRef;

    /**
     * Indicates that the concept's core representation is a non-enumerated text
     * format (as opposed to an enumeration/codelist).
     */
    boolean coreTextFormat;

    @Nullable
    String parentID;

    @Nullable
    String parentVersion;

    public Optional<CodelistRef> resolveRef(@Nullable CodelistRef localRef) {
        return Optional.ofNullable(localRef != null ? localRef : coreRef);
    }

    /**
     * Checks whether this concept defines a non-enumerated text format core
     * representation.
     *
     * @return true if the core representation is a text format, false otherwise
     */
    public boolean hasTextFormat() {
        return coreTextFormat;
    }
}
