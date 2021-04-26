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

import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataCursor;
import sdmxdl.Obs;
import sdmxdl.Series;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SeriesIterator implements Iterator<Series> {

    public static @NonNull Iterator<Series> of(@NonNull DataCursor cursor) {
        return cursor instanceof SeriesCursor
                ? ((SeriesCursor) cursor).delegate
                : new SeriesIterator(cursor);
    }

    @lombok.NonNull
    final DataCursor delegate;

    private final Series.Builder builder = Series.builder();
    private Series nextElement = null;

    private Series get() throws IOException {
        if (delegate.nextSeries()) {
            fill(builder, delegate);
            return builder.build();
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        if (nextElement != null) {
            return true;
        } else {
            try {
                nextElement = get();
                return nextElement != null;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public Series next() {
        if (nextElement != null || hasNext()) {
            Series line = nextElement;
            nextElement = null;
            return line;
        } else {
            throw new NoSuchElementException();
        }
    }

    private static void fill(Series.Builder builder, DataCursor cursor) throws IOException {
        builder.clearMeta()
                .clearObs()
                .key(cursor.getSeriesKey())
                .freq(cursor.getSeriesFrequency())
                .meta(cursor.getSeriesAttributes());
        Obs.Builder obs = Obs.builder();
        while (cursor.nextObs()) {
            builder.obs(obs
                    .clearMeta()
                    .period(cursor.getObsPeriod())
                    .value(cursor.getObsValue())
                    .meta(cursor.getObsAttributes())
                    .build()
            );
        }
    }
}
