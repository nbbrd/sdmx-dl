package sdmxdl;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

@RepresentableAsString
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.EqualsAndHashCode
public class Marker implements CharSequence {

    @lombok.experimental.Delegate(types = CharSequence.class)
    private final @NonNull String value;

    @Override
    public String toString() {
        return value;
    }

    @StaticFactoryMethod
    public static @NonNull Marker parse(@NonNull CharSequence value) {
        return new Marker(value.toString());
    }
}
