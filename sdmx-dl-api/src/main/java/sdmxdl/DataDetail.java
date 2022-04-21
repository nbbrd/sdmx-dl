package sdmxdl;

import lombok.AccessLevel;

/**
 * Describe an amount of information
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DataDetail {

    /**
     * All data and documentation, including annotations
     */
    FULL(true, true),
    /**
     * Attributes are excluded
     */
    DATA_ONLY(true, false),
    /**
     * Only the series elements and the dimensions that make up the series
     * keys
     */
    SERIES_KEYS_ONLY(false, false),
    /**
     * Groups and series, including attributes and annotations, without
     * observations
     */
    NO_DATA(false, true);

    @lombok.Getter
    private final boolean dataRequested;

    @lombok.Getter
    private final boolean metaRequested;
}
