package sdmxdl;

import lombok.NonNull;

import java.util.function.Consumer;

@FunctionalInterface
public interface EventListener<S extends SdmxSource> {

    void accept(@NonNull S source, @NonNull Marker marker, @NonNull CharSequence message);

    default @NonNull Consumer<CharSequence> asConsumer(@NonNull S source, @NonNull Marker marker) {
        return message -> accept(source, marker, message);
    }
}
