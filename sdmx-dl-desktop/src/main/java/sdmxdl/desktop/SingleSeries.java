package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.provider.ext.SeriesMeta;
import sdmxdl.provider.ext.SeriesMetaFactory;
import sdmxdl.web.SdmxWebManager;

import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;

import static internal.sdmxdl.desktop.Collectors2.single;

@lombok.Value
public class SingleSeries {

    @NonNull
    Structure dsd;

    @NonNull
    Series series;

    @NonNull
    Color accentColor;

    @lombok.Getter(lazy = true)
    @NonNull
    SeriesMeta meta = SeriesMetaFactory.getDefault(getDsd()).get(getSeries());

    @lombok.Getter(lazy = true)
    Duration duration = computeGlobalDuration();

    public static SingleSeries load(SdmxWebManager manager, DataSetRef ref, Color accentColor) throws IOException {
        try (Connection conn = ref.getDataSourceRef().getConnection(manager)) {
            return new SingleSeries(
                    conn.getStructure(ref.getDataSourceRef().getDatabase(), ref.getDataSourceRef().toFlowRef()),
                    conn.getDataStream(ref.getDataSourceRef().getDatabase(), ref.getDataSourceRef().toFlowRef(), Query.builder().key(ref.getKey()).build())
                            .findFirst()
                            .orElseGet(() -> Series.builder().key(ref.getKey()).build()),
                    accentColor
            );
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
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
