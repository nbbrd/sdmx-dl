package sdmxdl.ext;

import lombok.NonNull;
import sdmxdl.SdmxSource;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface SdmxSourceBiConsumer<S extends SdmxSource, T, U> {

    void accept(S source, T t, U u);

    default @NonNull BiConsumer<T, U> asBiConsumer(S source) {
        return (t, u) -> accept(source, t, u);
    }
}
