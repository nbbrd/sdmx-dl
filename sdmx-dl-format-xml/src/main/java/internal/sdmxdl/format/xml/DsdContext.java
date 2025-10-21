package internal.sdmxdl.format.xml;


import org.jspecify.annotations.Nullable;
import sdmxdl.Codelist;
import sdmxdl.CodelistRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@lombok.Getter
final class DsdContext {

    private final List<Codelist> codelists = new ArrayList<>();

    private final List<Concept> concepts = new ArrayList<>();

    private int dimensionCount = 0;

    public void incrementDimensionCount() {
        dimensionCount++;
    }

    public Optional<Codelist> findCodelistByRef(@Nullable CodelistRef ref) {
        return ref != null
                ? codelists.stream().filter(ref::containsRef).findFirst()
                : Optional.empty();
    }

    public Optional<Concept> findConceptById(@Nullable String id) {
        return id != null
                ? concepts.stream().filter(concept -> concept.getId().equals(id)).findFirst()
                : Optional.empty();
    }
}
