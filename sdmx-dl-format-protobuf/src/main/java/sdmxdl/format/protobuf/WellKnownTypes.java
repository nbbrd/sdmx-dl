package sdmxdl.format.protobuf;

import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@lombok.experimental.UtilityClass
class WellKnownTypes {

    public static Timestamp fromInstant(Instant value) {
        return Timestamp
                .newBuilder()
                .setSeconds(value.getEpochSecond())
                .setNanos(value.getNano())
                .build();
    }

    public static Instant toInstant(Timestamp value) {
        return Instant.ofEpochSecond(value.getSeconds(), value.getNanos());
    }

    public static Timestamp fromLocalDateTime(LocalDateTime value) {
        return Timestamp
                .newBuilder()
                .setSeconds(value.toEpochSecond(ZoneOffset.UTC))
                .setNanos(value.getNano())
                .build();
    }

    public static LocalDateTime toLocalDateTime(Timestamp value) {
        return LocalDateTime.ofEpochSecond(value.getSeconds(), value.getNanos(), ZoneOffset.UTC);
    }

    public static <X, Y> Iterable<? extends Y> fromCollection(Collection<X> collection, Function<X, Y> converter) {
        return () -> collection.stream().map(converter).iterator();
    }

    public static <X, Y> Collection<X> toCollection(Iterable<? extends Y> collection, Function<Y, X> converter) {
        return StreamSupport.stream(collection.spliterator(), false).map(converter).collect(Collectors.toList());
    }
}
