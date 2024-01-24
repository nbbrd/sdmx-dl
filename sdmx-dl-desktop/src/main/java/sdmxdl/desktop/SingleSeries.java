package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.provider.ext.SeriesMeta;
import sdmxdl.provider.ext.SeriesMetaFactory;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@lombok.Value
class SingleSeries {

    @NonNull Structure dsd;

    @NonNull Series series;

    @lombok.Getter(lazy = true)
    @NonNull SeriesMeta meta = SeriesMetaFactory.getDefault(getDsd()).get(getSeries());

    @lombok.Getter(lazy = true)
    Duration duration = computeGlobalDuration();

    public static SingleSeries load(SdmxWebManager manager, Languages languages, DataSetRef ref) throws IOException {
        try (Connection conn = manager.getConnection(ref.getDataSourceRef().getSource(), languages)) {
            return new SingleSeries(
                    conn.getStructure(ref.getDataSourceRef().getFlow()),
                    conn.getDataStream(ref.getDataSourceRef().getFlow(), Query.builder().key(ref.getKey()).build()).findFirst().orElseThrow(RuntimeException::new)
            );
        }
    }

    private Duration computeGlobalDuration() {
        List<Duration> collect = getSeries().getObs().stream().map(Obs::getPeriod).map(TimeInterval::getDuration).distinct().collect(Collectors.toList());
        return collect.size() == 1 ? collect.get(0) : null;
    }
}
