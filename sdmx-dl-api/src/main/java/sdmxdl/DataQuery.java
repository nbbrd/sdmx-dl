package sdmxdl;

import lombok.NonNull;

import java.util.stream.Stream;

@lombok.Value(staticConstructor = "of")
public class DataQuery {

    public static final DataQuery ALL = DataQuery.of(Key.ALL, DataDetail.FULL);

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
