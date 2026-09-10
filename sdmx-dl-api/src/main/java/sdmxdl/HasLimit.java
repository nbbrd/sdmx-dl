package sdmxdl;

import nbbrd.design.NonNegative;

/**
 * Exposes a maximum number of items to return.
 */
public interface HasLimit {

    /**
     * Gets the maximum number of results to return.
     *
     * @return a non-negative limit
     */
    @NonNegative int getMaxResults();

    /**
     * Default limit that means "no limit".
     */
    int NO_LIMIT = 0;
}
