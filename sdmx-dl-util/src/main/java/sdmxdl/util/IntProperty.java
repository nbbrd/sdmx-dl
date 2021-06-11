package sdmxdl.util;

import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

@lombok.RequiredArgsConstructor
public final class IntProperty extends BaseProperty {

    @lombok.NonNull
    @lombok.Getter
    private final String key;

    private final int defaultValue;

    public int get(@NonNull Map<?, ?> props) {
        Object value = props.get(key);
        if (value == null) return defaultValue;
        Integer result = Parser.onInteger().parse(value.toString());
        return result != null ? result : defaultValue;
    }
}
