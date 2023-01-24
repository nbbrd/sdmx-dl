package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.ext.Registry;
import sdmxdl.ext.SeriesMeta;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;

@lombok.Value
class SingleSeries {

    @NonNull DataStructure dsd;

    @NonNull Series series;

    @NonNull SeriesMeta meta;

    public static SingleSeries load(SdmxWebManager manager, Registry registry, DataSetRef ref) throws IOException {
        try (Connection conn = manager.getConnection(ref.getDataSourceRef().getSource())) {
            DataStructure dsd = conn.getStructure(ref.getDataSourceRef().getFlow());
            Series series = conn.getDataStream(ref.getDataSourceRef().getFlow(), DataQuery.builder().key(ref.getKey()).detail(DataDetail.FULL).build()).findFirst().orElseThrow(RuntimeException::new);
            return new SingleSeries(
                    dsd,
                    series,
                    loadSeriesMeta(manager, registry, ref, dsd, series)
            );
        }
    }

    private static SeriesMeta loadSeriesMeta(SdmxWebManager manager, Registry registry, DataSetRef ref, DataStructure dsd, Series series) {
        try {
            return registry.getFactory(manager, ref.getDataSourceRef().getSource(), dsd).apply(series);
        } catch (IOException ex) {
            System.out.println("Not found");
            return SeriesMeta.EMPTY;
        }
    }
}
