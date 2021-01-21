package internal.sdmxdl.ri.file;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;

@lombok.RequiredArgsConstructor
public final class FilteredDataCursor implements DataCursor {

    @lombok.NonNull
    private final DataCursor delegate;

    @lombok.NonNull
    private final Key key;

    @Override
    public boolean nextSeries() throws IOException {
        while (delegate.nextSeries()) {
            if (key.contains(delegate.getSeriesKey())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NonNull Key getSeriesKey() throws IOException, IllegalStateException {
        return delegate.getSeriesKey();
    }

    @Override
    public @NonNull Frequency getSeriesFrequency() throws IOException, IllegalStateException {
        return delegate.getSeriesFrequency();
    }

    @Override
    public @Nullable String getSeriesAttribute(@NonNull String key) throws IOException, IllegalStateException {
        return delegate.getSeriesAttribute(key);
    }

    @Override
    public @NonNull Map<String, String> getSeriesAttributes() throws IOException, IllegalStateException {
        return delegate.getSeriesAttributes();
    }

    @Override
    public boolean nextObs() throws IOException, IllegalStateException {
        return delegate.nextObs();
    }

    @Override
    public @Nullable LocalDateTime getObsPeriod() throws IOException, IllegalStateException {
        return delegate.getObsPeriod();
    }

    @Override
    public @Nullable Double getObsValue() throws IOException, IllegalStateException {
        return delegate.getObsValue();
    }

    @Override
    public @NonNull Stream<Series> toStream(DataFilter.@NonNull Detail detail) throws IOException, IllegalStateException {
        return delegate.toStream(detail).filter(key::containsKey);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
