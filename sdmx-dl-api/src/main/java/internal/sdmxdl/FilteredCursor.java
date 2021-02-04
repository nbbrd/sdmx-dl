package internal.sdmxdl;

import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataCursor;
import sdmxdl.DataFilter;
import sdmxdl.Frequency;
import sdmxdl.Key;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilteredCursor implements DataCursor {

    public static @NonNull DataCursor of(@NonNull DataCursor cursor, @NonNull Key key, @NonNull DataFilter filter) {
        return isNoFilter(key, filter) ? cursor : new FilteredCursor(cursor, key, filter);
    }

    @lombok.NonNull
    private final DataCursor delegate;

    @lombok.NonNull
    private final Key key;

    @lombok.NonNull
    private final DataFilter filter;

    private boolean seriesAvailable;
    private boolean obsAvailable;
    private boolean closed = false;

    @Override
    public boolean nextSeries() throws IOException {
        checkState();
        seriesAvailable = false;
        obsAvailable = false;
        while (delegate.nextSeries()) {
            if (key.contains(delegate.getSeriesKey())) {
                seriesAvailable = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public @NonNull Key getSeriesKey() throws IOException, IllegalStateException {
        checkSeriesState();
        return delegate.getSeriesKey();
    }

    @Override
    public @NonNull Frequency getSeriesFrequency() throws IOException, IllegalStateException {
        checkSeriesState();
        return delegate.getSeriesFrequency();
    }

    @Override
    public @Nullable String getSeriesAttribute(@NonNull String key) throws IOException, IllegalStateException {
        checkSeriesState();
        String result = delegate.getSeriesAttribute(key);
        return filter.getDetail().isMetaRequested() ? result : null;
    }

    @Override
    public @NonNull Map<String, String> getSeriesAttributes() throws IOException, IllegalStateException {
        checkSeriesState();
        Map<String, String> result = delegate.getSeriesAttributes();
        return filter.getDetail().isMetaRequested() ? result : Collections.emptyMap();
    }

    @Override
    public boolean nextObs() throws IOException, IllegalStateException {
        checkSeriesState();
        return obsAvailable = (delegate.nextObs() && filter.getDetail().isDataRequested());
    }

    @Override
    public @Nullable LocalDateTime getObsPeriod() throws IOException, IllegalStateException {
        checkObsState();
        return delegate.getObsPeriod();
    }

    @Override
    public @Nullable Double getObsValue() throws IOException, IllegalStateException {
        checkObsState();
        return delegate.getObsValue();
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
        delegate.close();
    }

    private void checkState() throws IOException {
        if (closed) {
            throw new IOException("Cursor closed");
        }
    }

    private void checkSeriesState() throws IOException, IllegalStateException {
        checkState();
        if (!seriesAvailable) {
            throw new IllegalStateException();
        }
    }

    private void checkObsState() throws IOException, IllegalStateException {
        checkSeriesState();
        if (!obsAvailable) {
            throw new IllegalStateException();
        }
    }

    public static boolean isNoFilter(@NonNull Key key, @NonNull DataFilter filter) {
        return Key.ALL.equals(key) && DataFilter.ALL.equals(filter);
    }
}
