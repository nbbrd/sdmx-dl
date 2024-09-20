package sdmxdl;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CatalogRef {

    @NonNull
    String id;

    @Override
    public String toString() {
        return id;
    }

    @StaticFactoryMethod
    public static @NonNull CatalogRef parse(@NonNull CharSequence input) throws IllegalArgumentException {
        return new CatalogRef(input.toString());
    }

    public static final CatalogRef NO_CATALOG = CatalogRef.parse("");
}
