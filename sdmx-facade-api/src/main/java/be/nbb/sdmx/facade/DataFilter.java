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
package be.nbb.sdmx.facade;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class DataFilter {

    public static final DataFilter ALL = builder().build();
    public static final DataFilter SERIES_KEYS_ONLY = builder().detail(Detail.SERIES_KEYS_ONLY).build();

    @lombok.NonNull
    Detail detail;

    // Fix lombok.Builder.Default bug in NetBeans
    public static Builder builder() {
        return new Builder()
                .detail(Detail.FULL);
    }

    public boolean isSeriesKeyOnly() {
        return detail.equals(Detail.SERIES_KEYS_ONLY);
    }

    public enum Detail {
        FULL, SERIES_KEYS_ONLY
    }
}
