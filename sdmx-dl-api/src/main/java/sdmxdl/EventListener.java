package sdmxdl;

import lombok.NonNull;

@FunctionalInterface
public interface EventListener {

    void accept(@NonNull String marker, @NonNull CharSequence message);

    default void accept(@NonNull String marker, @NonNull CharSequence message, int depth) {
        accept(marker, message);
    }
}
