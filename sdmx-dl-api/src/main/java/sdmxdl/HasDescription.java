package sdmxdl;

import org.checkerframework.checker.nullness.qual.Nullable;

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
