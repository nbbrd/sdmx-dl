package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.Key;
import sdmxdl.KeyRequest;

@lombok.Value
@lombok.Builder
public class DataSetRef {

    @NonNull
    DataSourceRef dataSourceRef;

    @NonNull
    Key key;

    int dimensionIndex;

    public KeyRequest toKeyRequest() {
        return KeyRequest
                .builderOf(dataSourceRef.toFlowRequest())
                .key(key)
                .build();
    }
}
