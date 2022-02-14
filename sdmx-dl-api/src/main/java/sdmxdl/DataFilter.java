/*
 * Copyright 2017 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package sdmxdl;

import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class DataFilter {

    public static final DataFilter FULL = builder().detail(Detail.FULL).build();
    public static final DataFilter SERIES_KEYS_ONLY = builder().detail(Detail.SERIES_KEYS_ONLY).build();
    public static final DataFilter NO_DATA = builder().detail(Detail.NO_DATA).build();
    public static final DataFilter DATA_ONLY = builder().detail(Detail.DATA_ONLY).build();

    /**
     * Specifies the desired amount of information to be returned
     */
    @lombok.NonNull
    @lombok.Builder.Default
    Detail detail = Detail.FULL;

    /**
     * Describe an amount of information
     */
    @lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Detail {
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

    public @NonNull Series apply(@NonNull Series series) {
        if (detail.isDataRequested()) {
            if (detail.isMetaRequested()) {
                return series;
            } else {
                return series.toBuilder().clearMeta().build();
            }
        } else {
            if (detail.isMetaRequested()) {
                return series.toBuilder().clearObs().build();
            } else {
                return series.toBuilder().clearObs().clearMeta().build();
            }
        }
    }
}
