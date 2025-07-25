package sdmxdl.format;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.HasExpiration;
import sdmxdl.ext.Cache;

import java.time.Clock;

@lombok.AllArgsConstructor
public final class DualCache<V extends HasExpiration> implements Cache<V> {

    private final @NonNull Cache<V> first;
    private final @NonNull Cache<V> second;
    private final @NonNull Clock clock;

    @Override
    public @NonNull Clock getClock() {
        return clock;
    }

    @Override
    public @Nullable V get(@NonNull String key) {
        V result = first.get(key);
        if (result == null) {
            result = second.get(key);
            if (result != null) {
                first.put(key, result);
            }
        }
        return result;
    }

    @Override
    public void put(@NonNull String key, @NonNull V value) {
        first.put(key, value);
        second.put(key, value);
    }
}
