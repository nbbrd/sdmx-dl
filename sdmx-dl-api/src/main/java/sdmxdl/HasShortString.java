package sdmxdl;

import lombok.NonNull;

/**
 * Defines the ability to be represented as a short string.
 */
public interface HasShortString {

    @NonNull
    String toShortString();
}
