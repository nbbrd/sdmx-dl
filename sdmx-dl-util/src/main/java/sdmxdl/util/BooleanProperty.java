package sdmxdl.util;

import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

@lombok.RequiredArgsConstructor
public final class BooleanProperty extends BaseProperty {

    @lombok.NonNull
    @lombok.Getter
    private final String key;

    private final boolean defaultValue;

    public boolean get(@NonNull Map<?, ?> props) {
        Object value = props.get(key);
        if (value == null) return defaultValue;
        Boolean result = Parser.onBoolean().parse(value.toString());
        return result != null ? result : defaultValue;
    }
}
