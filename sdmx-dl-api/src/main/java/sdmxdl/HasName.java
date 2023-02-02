package sdmxdl;

import lombok.NonNull;

public interface HasName {

    /**
     * Gets a human-readable (best-language-match) name.
     *
     * @return a non-null name
     */
    @NonNull String getName();
}
