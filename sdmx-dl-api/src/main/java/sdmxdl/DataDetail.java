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
    FULL(false, false),
    /**
     * Attributes are excluded
     */
    DATA_ONLY(false, true),
    /**
     * Only the series elements and the dimensions that make up the series
     * keys
     */
    SERIES_KEYS_ONLY(true, true),
    /**
     * Groups and series, including attributes and annotations, without
     * observations
     */
    NO_DATA(true, false);

    @lombok.Getter
    private final boolean ignoreData;

    @lombok.Getter
    private final boolean ignoreMeta;
}
