package sdmxdl;

import lombok.NonNull;

/**
 * Defines the ability to have a human-readable name.
 */
public interface HasName {

    /**
     * Gets a human-readable (best-language-match) name.
     *
     * @return a non-null non-blank name
     * @apiNote The SDMX information model considers names as optional in some cases.
     * sdmx-dl on the other hand makes names always mandatory.
     */
    @NonNull String getName();
}
