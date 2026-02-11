package sdmxdl.provider.caching;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.HasExpiration;
import sdmxdl.ext.Cache;

import java.time.Clock;
import java.util.function.Predicate;
import java.util.function.Supplier;

@lombok.Builder
public final class DualCache<V extends HasExpiration> implements Cache<V> {

    private final @NonNull Cache<V> first;
    private final @NonNull Cache<V> second;
    private final @NonNull Clock clock;

    @lombok.Builder.Default
    private final @NonNull Predicate<@NonNull V> nullObjectPredicate = ignore -> false;

    @lombok.Builder.Default
    private final @NonNull Supplier<@Nullable V> nullObjectSupplier = () -> null;

    @Override
    public @NonNull Clock getClock() {
        return clock;
    }

    @Override
    public @Nullable V get(@NonNull String key) {
        V result = first.get(key);
        if (result == null) {
            result = second.get(key);
            if (result == null) {
                result = nullObjectSupplier.get();
            }
            if (result != null) {
                first.put(key, result);
            }
        }
        return result != null && !nullObjectPredicate.test(result) ? result : null;
    }

    @Override
    public void put(@NonNull String key, @Nullable V value) {
        first.put(key, value);
        second.put(key, value);
    }
}
