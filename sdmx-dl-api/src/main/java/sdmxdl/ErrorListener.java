package sdmxdl;

import lombok.NonNull;

import java.io.IOException;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ErrorListener {

    void accept(@NonNull String marker, @NonNull CharSequence message, @NonNull IOException error);

    default @NonNull BiConsumer<CharSequence, IOException> asBiConsumer(@NonNull String marker) {
        return (message, error) -> accept(marker, message, error);
    }
}
