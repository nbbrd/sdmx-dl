package sdmxdl.ext;

import lombok.NonNull;
import sdmxdl.SdmxSource;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@FunctionalInterface
public interface SdmxSourceConsumer<S extends SdmxSource, T> extends BiConsumer<S, T> {

    default @NonNull Consumer<T> asConsumer(S source) {
        return t -> accept(source, t);
    }
}
