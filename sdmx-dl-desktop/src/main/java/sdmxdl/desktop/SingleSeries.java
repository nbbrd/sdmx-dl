package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.provider.ext.SeriesMeta;
import sdmxdl.provider.ext.SeriesMetaFactory;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;

import static internal.sdmxdl.desktop.Collectors2.single;

@lombok.Value
public class SingleSeries {

    @NonNull
    Structure dsd;

    @NonNull
    Series series;

    @lombok.Getter(lazy = true)
    @NonNull
    SeriesMeta meta = SeriesMetaFactory.getDefault(getDsd()).get(getSeries());

    @lombok.Getter(lazy = true)
    Duration duration = computeGlobalDuration();

    public static SingleSeries load(SdmxWebManager manager, DataSetRef ref) throws IOException {
        try (Connection conn = manager.getConnection(ref.getDataSourceRef().getSource(), ref.getDataSourceRef().toOptions())) {
            return new SingleSeries(
                    conn.getStructure(ref.getDataSourceRef().getFlow()),
                    conn.getDataStream(ref.getDataSourceRef().getFlow(), Query.builder().key(ref.getKey()).build())
                            .findFirst()
                            .orElseGet(() -> Series.builder().key(ref.getKey()).build())
            );
        }
    }

    private Duration computeGlobalDuration() {
        return getSeries()
                .getObs()
                .stream()
                .map(Obs::getPeriod)
                .map(TimeInterval::getDuration)
                .distinct()
                .collect(single())
                .orElse(null);
    }
}
