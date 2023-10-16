package sdmxdl.provider;

import lombok.NonNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@lombok.experimental.UtilityClass
public class Suppliers {

    public static <T> @NonNull Supplier<T> memoize(@NonNull Supplier<T> supplier) {
        AtomicReference<T> ref = new AtomicReference<>();
        return () -> {
            T result = ref.get();
            if (result == null) {
                result = supplier.get();
                ref.set(result);
            }
            return result;
        };
    }
}
