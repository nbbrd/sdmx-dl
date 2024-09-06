package internal.sdmxdl.desktop;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public final class Collectors2 {

    private Collectors2() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <T> Collector<T, ?, Optional<T>> single() {
        return collectingAndThen(toList(), Collectors2::getSingle);
    }

    public static <T> Optional<T> getSingle(List<T> list) {
        return list.size() == 1 ? Optional.ofNullable(list.get(0)) : Optional.empty();
    }
}
