package sdmxdl;

import lombok.NonNull;

import java.util.stream.Stream;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class DataQuery {

    public static final DataQuery ALL = DataQuery.builder().build();

    @lombok.NonNull
    @lombok.Builder.Default
    Key key = Key.ALL;

    @lombok.NonNull
    @lombok.Builder.Default
    DataDetail detail = DataDetail.FULL;

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
