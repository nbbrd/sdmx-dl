package sdmxdl;

import lombok.NonNull;

import java.io.IOException;

@FunctionalInterface
public interface ErrorListener {

    void accept(@NonNull String marker, @NonNull CharSequence message, @NonNull IOException error);
}
