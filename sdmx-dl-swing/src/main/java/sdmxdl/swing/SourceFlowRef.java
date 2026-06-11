package sdmxdl.swing;

import lombok.NonNull;
import sdmxdl.DatabaseRef;
import sdmxdl.FlowRef;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class SourceFlowRef {

    @NonNull
    String source;

    @lombok.Builder.Default
    @NonNull
    DatabaseRef database = DatabaseRef.NO_DATABASE;

    @NonNull
    FlowRef flow;
}
