package sdmxdl.format.protobuf;

import java.time.Instant;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@lombok.experimental.UtilityClass
class WellKnownTypes {

    public static String fromInstant(Instant value) {
        return value.toString();
    }

    public static Instant toInstant(String value) {
        return Instant.parse(value);
    }

    public static <X, Y> Iterable<? extends Y> fromCollection(Collection<X> collection, Function<X, Y> converter) {
        return () -> collection.stream().map(converter).iterator();
    }

    public static <X, Y> Collection<X> toCollection(Iterable<? extends Y> collection, Function<Y, X> converter) {
        return StreamSupport.stream(collection.spliterator(), false).map(converter).collect(Collectors.toList());
    }
}
