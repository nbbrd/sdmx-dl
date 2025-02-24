package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.Connection;
import sdmxdl.Flow;
import sdmxdl.Structure;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;

@lombok.Value
public class FlowStruct {

    @NonNull
    Flow flow;

    @NonNull
    Structure structure;

    public static FlowStruct load(SdmxWebManager manager, DataSourceRef ref) throws IOException {
        try (Connection conn = ref.getConnection(manager)) {
            return new FlowStruct(conn.getFlow(ref.getDatabase(), ref.toFlowRef()), conn.getStructure(ref.getDatabase(), ref.toFlowRef()));
        }
    }
}
