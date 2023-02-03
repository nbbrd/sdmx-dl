package sdmxdl;

import lombok.NonNull;

import java.util.stream.Stream;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class DataQuery {

    public static final DataQuery ALL = DataQuery.builder().key(Key.ALL).detail(DataDetail.FULL).build();

    @lombok.NonNull
    Key key;

    @lombok.NonNull
    DataDetail detail;

    public @NonNull Stream<Series> execute(@NonNull Stream<Series> data) {
        return data
                .filter(key::containsKey)
                .map(this::map);
    }

    private Series map(Series series) {
        if (detail.isIgnoreData()) {
            if (detail.isIgnoreMeta()) {
                return series.toBuilder().clearObs().clearMeta().build();
            } else {
                return series.toBuilder().clearObs().build();
            }
        } else {
            if (detail.isIgnoreMeta()) {
                return series.toBuilder().clearMeta().build();
            } else {
                return series;
            }
        }
    }
}
