/*
 * Copyright 2015 National Bank of Belgium
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
package sdmxdl.xml;

import internal.sdmxdl.xml.SeriesIterator;
import nbbrd.design.NotThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.Key;
import sdmxdl.Series;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Philippe Charles
 */
@NotThreadSafe
public interface DataCursor extends Closeable {

    boolean nextSeries() throws IOException;

    @NonNull
    Key getSeriesKey() throws IOException, IllegalStateException;

    @Nullable
    String getSeriesAttribute(@NonNull String key) throws IOException, IllegalStateException;

    @NonNull
    Map<String, String> getSeriesAttributes() throws IOException, IllegalStateException;

    boolean nextObs() throws IOException, IllegalStateException;

    @Nullable
    LocalDateTime getObsPeriod() throws IOException, IllegalStateException;

    @Nullable
    Double getObsValue() throws IOException, IllegalStateException;

    @NonNull
    Map<String, String> getObsAttributes() throws IOException, IllegalStateException;

    @NonNull
    default Stream<Series> toStream() {
        Iterator<Series> iterator = new SeriesIterator(this);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.NONNULL), false);
    }

    @NonNull
    default Stream<Series> toCloseableStream() {
        return toStream().onClose(() -> {
            try {
                DataCursor.this.close();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
    }
}
