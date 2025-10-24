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

    public Optional<CodelistRef> resolveRef(@Nullable CodelistRef localRef) {
        return Optional.ofNullable(localRef != null ? localRef : coreRef);
    }
}
