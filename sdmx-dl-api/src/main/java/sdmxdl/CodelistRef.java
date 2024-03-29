package sdmxdl;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.Nullable;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.EqualsAndHashCode(callSuper = false)
public class CodelistRef extends ResourceRef<CodelistRef> {

    @lombok.NonNull
    String agency;

    @lombok.NonNull
    String id;

    @lombok.NonNull
    String version;

    @Override
    public String toString() {
        return toString(this);
    }

    @StaticFactoryMethod
    public static @NonNull CodelistRef parse(@NonNull CharSequence input) throws IllegalArgumentException {
        return create(input, CodelistRef::new);
    }

    @StaticFactoryMethod
    public static @NonNull CodelistRef of(@Nullable String agency, @NonNull String id, @Nullable String version) throws IllegalArgumentException {
        return of(agency, id, version, CodelistRef::new);
    }
}
