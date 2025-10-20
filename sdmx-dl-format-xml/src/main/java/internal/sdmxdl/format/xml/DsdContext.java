package internal.sdmxdl.format.xml;


import lombok.NonNull;
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

    public Optional<Codelist> getCodelist(@NonNull CodelistRef ref) {
        return codelists
                .stream()
                .filter(ref::containsRef)
                .findFirst();
    }

    public Optional<Concept> getConcept(@NonNull String id) {
        return concepts
                .stream()
                .filter(concept -> concept.getId().equals(id))
                .findFirst();
    }
}
