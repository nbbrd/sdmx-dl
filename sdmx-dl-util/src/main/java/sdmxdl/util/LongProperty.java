package sdmxdl.util;

import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

@lombok.RequiredArgsConstructor
public final class LongProperty extends BaseProperty {

    @lombok.NonNull
    @lombok.Getter
    private final String key;

    private final long defaultValue;

    public long get(@NonNull Map<?, ?> props) {
        Object value = props.get(key);
        if (value == null) return defaultValue;
        Long result = Parser.onLong().parse(value.toString());
        return result != null ? result : defaultValue;
    }
}
