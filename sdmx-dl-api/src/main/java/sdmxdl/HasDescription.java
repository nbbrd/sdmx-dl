package sdmxdl;

import lombok.NonNull;

public interface HasDescription {

    /**
     * Gets a human-readable (best-language-match) description.
     *
     * @return a non-null description
     */
    @NonNull String getDescription();
}
