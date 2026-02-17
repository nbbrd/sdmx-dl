package sdmxdl;

import lombok.NonNull;

@FunctionalInterface
public interface EventListener {

    void accept(@NonNull String marker, @NonNull CharSequence message);
}
