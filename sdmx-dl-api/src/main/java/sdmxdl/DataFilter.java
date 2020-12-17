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

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class DataFilter {

    public static final DataFilter ALL = builder().build();
    public static final DataFilter SERIES_KEYS_ONLY = builder().detail(Detail.SERIES_KEYS_ONLY).build();

    /**
     * Specifies the desired amount of information to be returned
     */
    @lombok.NonNull
    @lombok.Builder.Default
    Detail detail = Detail.FULL;

    public boolean isSeriesKeyOnly() {
        return detail.equals(Detail.SERIES_KEYS_ONLY);
    }

    /**
     * Describe an amount of information
     */
    public enum Detail {
        /**
         * All data and documentation, including annotations
         */
        FULL,
        /**
         * Attributes are excluded
         */
        DATA_ONLY,
        /**
         * Only the series elements and the dimensions that make up the series
         * keys
         */
        SERIES_KEYS_ONLY,
        /**
         * Groups and series, including attributes and annotations, without
         * observations
         */
        NO_DATA
    }
}
