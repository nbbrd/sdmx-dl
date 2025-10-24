package sdmxdl;

import org.jspecify.annotations.Nullable;

/**
 * Defines the ability to have a human-readable description.
 */
public interface HasDescription {

    /**
     * Gets an optional human-readable (best-language-match) description.
     *
     * @return a nullable description
     */
    @Nullable String getDescription();
}
