package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.DataflowRef;

import java.util.List;

@lombok.Value
public class DataSourceRef {

    @NonNull String source;

    @NonNull DataflowRef flow;

    @NonNull List<String> dimensions;
}
