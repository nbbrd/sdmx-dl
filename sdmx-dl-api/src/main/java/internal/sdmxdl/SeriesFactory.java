/*
 * Copyright 2019 National Bank of Belgium
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
package internal.sdmxdl;

import sdmxdl.DataCursor;
import sdmxdl.DataFilter;
import sdmxdl.Obs;
import sdmxdl.Series;

import java.io.IOException;
import java.util.Objects;

/**
 *
 * @author Philippe Charles
 */
public enum SeriesFactory {
    FULL {
        @Override
        void fill(Series.Builder builder, DataCursor cursor) throws IOException {
            fillHeader(builder, cursor);
            fillAttributes(builder, cursor);
            fillValues(builder, cursor);
        }
    }, NO_DATA {
        @Override
        void fill(Series.Builder builder, DataCursor cursor) throws IOException {
            fillHeader(builder, cursor);
            fillAttributes(builder, cursor);
        }
    }, DATA_ONLY {
        @Override
        void fill(Series.Builder builder, DataCursor cursor) throws IOException {
            fillHeader(builder, cursor);
            fillValues(builder, cursor);
        }
    }, SERIES_KEYS_ONLY {
        @Override
        void fill(Series.Builder builder, DataCursor cursor) throws IOException {
            fillHeader(builder, cursor);
        }
    };

    abstract void fill(Series.Builder builder, DataCursor cursor) throws IOException;

    private static void fillHeader(Series.Builder builder, DataCursor cursor) throws IOException {
        builder.key(cursor.getSeriesKey()).freq(cursor.getSeriesFrequency());
    }

    private static void fillAttributes(Series.Builder builder, DataCursor cursor) throws IOException {
        builder.clearMeta().meta(cursor.getSeriesAttributes());
    }

    private static void fillValues(Series.Builder builder, DataCursor cursor) throws IOException {
        builder.clearObs();
        while (cursor.nextObs()) {
            builder.obs(Obs.of(cursor.getObsPeriod(), cursor.getObsValue()));
        }
    }

    public static SeriesFactory of(DataFilter.Detail detail) {
        Objects.requireNonNull(detail);
        switch (detail) {
            case FULL:
                return FULL;
            case NO_DATA:
                return NO_DATA;
            case DATA_ONLY:
                return DATA_ONLY;
            case SERIES_KEYS_ONLY:
                return SERIES_KEYS_ONLY;
            default:
                throw new RuntimeException();
        }
    }
}
