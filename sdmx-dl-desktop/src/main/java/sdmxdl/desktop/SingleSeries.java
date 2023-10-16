package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.provider.ext.SeriesMeta;
import sdmxdl.provider.ext.SeriesMetaFactory;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;

@lombok.Value
class SingleSeries {

    @NonNull Structure dsd;

    @NonNull Series series;

    @NonNull SeriesMeta meta;

    public static SingleSeries load(SdmxWebManager manager, Languages languages, DataSetRef ref) throws IOException {
        try (Connection conn = manager.getConnection(ref.getDataSourceRef().getSource(), languages)) {
            Structure dsd = conn.getStructure(ref.getDataSourceRef().getFlow());
            Series series = conn.getDataStream(ref.getDataSourceRef().getFlow(), Query.builder().key(ref.getKey()).detail(Detail.FULL).build()).findFirst().orElseThrow(RuntimeException::new);
            return new SingleSeries(
                    dsd,
                    series,
                    SeriesMetaFactory.getDefault(dsd).get(series)
            );
        }
    }
}
