package sdmxdl;

import lombok.NonNull;

import java.io.IOException;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ErrorListener<S extends SdmxSource> {

    void accept(@NonNull S source, @NonNull String marker, @NonNull CharSequence message, @NonNull IOException error);

    default @NonNull BiConsumer<CharSequence, IOException> asBiConsumer(@NonNull S source, @NonNull String marker) {
        return (message, error) -> accept(source, marker, message, error);
    }
}
