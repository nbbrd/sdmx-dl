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
        try (Connection conn = manager.getConnection(ref.getSource(), ref.getLanguages())) {
            return new FlowStruct(conn.getFlow(ref.getCatalog(), ref.toFlowRef()), conn.getStructure(ref.getCatalog(), ref.toFlowRef()));
        }
    }
}
