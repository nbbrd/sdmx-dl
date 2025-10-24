package sdmxdl;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

@RepresentableAsString
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatabaseRef {

    @NonNull
    String id;

    @Override
    public String toString() {
        return id;
    }

    @StaticFactoryMethod
    public static @NonNull DatabaseRef parse(@NonNull CharSequence input) throws IllegalArgumentException {
        return new DatabaseRef(input.toString());
    }

    public static final String NO_DATABASE_KEYWORD = "";
    public static final DatabaseRef NO_DATABASE = DatabaseRef.parse(NO_DATABASE_KEYWORD);
}
