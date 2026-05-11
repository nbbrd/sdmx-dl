package internal.sdmxdl.format.xml;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@lombok.Value
class ConceptIdentity {

    @Nullable
    String maintainableParentID;

    @Nullable
    String maintainableParentVersion;

    @Nullable
    String agencyID;

    @NonNull
    String id;

    public boolean isCompatibleWith(@NonNull Concept concept) {
        return id.equals(concept.getId())
                && Objects.equals(maintainableParentID, concept.getParentID())
                && Objects.equals(maintainableParentVersion, concept.getParentVersion());
    }
}
