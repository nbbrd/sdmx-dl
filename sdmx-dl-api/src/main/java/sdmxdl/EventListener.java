package sdmxdl;

import lombok.NonNull;

import java.util.function.Consumer;

@FunctionalInterface
public interface EventListener {

    void accept(@NonNull String marker, @NonNull CharSequence message);

    default @NonNull Consumer<CharSequence> asConsumer(@NonNull String marker) {
        return message -> accept(marker, message);
    }
}
