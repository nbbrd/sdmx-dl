package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.FlowRef;

import java.util.List;

@lombok.Value
public class DataSourceRef {

    @NonNull String source;

    @NonNull FlowRef flow;

    @NonNull List<String> dimensions;
}
