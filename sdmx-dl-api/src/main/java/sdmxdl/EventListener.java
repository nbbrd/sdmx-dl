package sdmxdl;

import lombok.NonNull;

import java.util.function.Consumer;

@FunctionalInterface
public interface EventListener<S extends SdmxSource> {

    void accept(@NonNull S source, @NonNull String marker, @NonNull CharSequence message);

    default @NonNull Consumer<CharSequence> asConsumer(@NonNull S source, @NonNull String marker) {
        return message -> accept(source, marker, message);
    }
}
