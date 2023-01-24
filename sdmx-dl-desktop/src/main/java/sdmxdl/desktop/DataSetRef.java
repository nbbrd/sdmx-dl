package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.Key;

@lombok.Value
public class DataSetRef {

    @NonNull DataSourceRef dataSourceRef;

    @NonNull Key key;

    int dimensionIndex;
}
