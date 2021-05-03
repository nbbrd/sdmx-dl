package sdmxdl.tck;

import org.assertj.core.api.AbstractAssert;
import sdmxdl.Key;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeyAssert extends AbstractAssert<KeyAssert, Key> {

    public KeyAssert(Key actual) {
        super(actual, KeyAssert.class);
    }

    public static List<Key> keys(String... keys) {
        return Stream.of(keys).map(Key::parse).collect(Collectors.toList());
    }
}
