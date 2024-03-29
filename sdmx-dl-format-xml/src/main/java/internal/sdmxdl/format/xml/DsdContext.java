package internal.sdmxdl.format.xml;


import sdmxdl.Codelist;
import sdmxdl.CodelistRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@lombok.Getter
final class DsdContext {

    private final List<Codelist> codelists = new ArrayList<>();

    private final Map<String, String> concepts = new HashMap<>();

    private int dimensionCount = 0;

    public void incrementDimensionCount() {
        dimensionCount++;
    }

    public Codelist getCodelist(CodelistRef ref) {
        return codelists
                .stream()
                .filter(ref::containsRef)
                .findFirst()
                .orElseGet(() -> Codelist.builder().ref(ref).build());
    }
}
