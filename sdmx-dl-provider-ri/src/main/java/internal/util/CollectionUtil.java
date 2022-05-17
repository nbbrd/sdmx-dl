package internal.util;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@lombok.experimental.UtilityClass
public class CollectionUtil {

    public static <K, V> Map<K, V> zip(Collection<K> keys, Collection<V> values) {
        Map<K, V> result = new HashMap<>();
        Iterator<K> keysIter = keys.iterator();
        Iterator<V> valuesIter = values.iterator();
        for (int i = 0; i < keys.size(); i++) {
            result.put(keysIter.next(), valuesIter.next());
        }
        return result;
    }

    public static <X> Stream<IndexedElement<X>> indexedStreamOf(List<X> list) {
        return IntStream.range(0, list.size()).mapToObj(index -> new IndexedElement<>(index, list.get(index)));
    }

    @lombok.Value
    public static class IndexedElement<T> {

        int index;
        T element;
    }
}
